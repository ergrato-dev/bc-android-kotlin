# Trabajo en segundo plano y WorkManager

> En la semana 31, «Actualizar» hablaba con el servidor mientras la persona miraba la pantalla. Si no había red, lo veía en un aviso y podía volver a tocar el botón. Pero un cambio hecho en el campo, sin cobertura, también tiene que llegar al servidor: cuando vuelva la red, quizá con la app cerrada y el móvil en el bolsillo. Eso es trabajo en segundo plano, y en Android tiene reglas. Esta teoría cuenta qué hace el sistema con una app que no está en pantalla y qué herramienta usar para el trabajo que tiene que acabar sí o sí: WorkManager.

## 🎯 Objetivos

- Saber qué le pasa a una corrutina cuando la app sale de la pantalla
- Elegir entre una corrutina, un servicio en primer plano y WorkManager
- Escribir un `CoroutineWorker` y devolver `success`, `retry` o `failure`
- Pasar datos a un trabajo y leer los que devuelve

## 1. La app que no está en pantalla

Cuando la persona sale de la Bitácora, Android no cierra el proceso enseguida, pero tampoco promete nada: lo puede matar cuando necesite memoria, y mientras tanto lo frena. Con la pantalla apagada y el móvil quieto, el modo *Doze* agrupa las alarmas, corta la red y aplaza los trabajos a ventanas de mantenimiento. Las apps que se usan poco caen en cubos de *App Standby* que se despiertan aún menos.

Lo que eso significa para cada herramienta:

| Herramienta | Vive lo que vive | Si el proceso muere | Para |
| --- | --- | --- | --- |
| `viewModelScope.launch` | la pantalla (el ViewModel) | se pierde | lo que la persona espera delante de la pantalla: «Actualizar» |
| Servicio en primer plano | lo que diga la app, con una notificación visible | se pierde | lo que la persona sabe que está pasando: una descarga grande, una ruta con GPS |
| WorkManager | lo que tarde en acabar, aunque la app se cierre o el móvil se reinicie | se reprograma | lo que tiene que acabar alguna vez, cuando se pueda: subir los cambios |

La sincronización de la Bitácora es de la tercera fila: no tiene prisa, no necesita que nadie mire, y no se puede perder.

## 2. Qué es WorkManager

WorkManager guarda cada trabajo en una base de datos propia antes de hacer nada. Por eso sobrevive a todo: si el proceso muere, al volver a arrancar lee su base de datos y sigue; si el móvil se reinicia, un receptor de `BOOT_COMPLETED` vuelve a programar lo pendiente. Por debajo usa el `JobScheduler` del sistema, que respeta Doze y la batería, y mientras la app está viva, también ejecuta él mismo lo que ya puede correr.

No es para todo:

- **No es inmediato.** El sistema decide cuándo, dentro de las condiciones que pongas.
- **No es exacto.** Para «a las 8:00 en punto» está `AlarmManager`.
- **No es para trabajo que muere con la pantalla.** Si al cerrar la pantalla ya no sirve, una corrutina en `viewModelScope`.

> 🔬 Medido con WorkManager 2.12.0 en la Bitácora: `work-runtime` añade al APK tres permisos, de su propio manifiesto: `RECEIVE_BOOT_COMPLETED` (reprogramar tras reiniciar), `WAKE_LOCK` (mantener la CPU despierta mientras corre) y `FOREGROUND_SERVICE` (para los trabajos largos que se hacen visibles). Los tres son normales: no se piden a la persona. El `BuildTest` de la raíz los cuenta.

> 🔬 Medido también: WorkManager guarda sus trabajos con **Room 2** (`androidx.room:room-runtime` 2.7.0, con `androidx.sqlite` 2.5.0 que Gradle sube a 2.7.1). La Bitácora usa Room 3 (`androidx.room3`): los dos conviven en el APK porque son paquetes distintos. El APK `field` de release pasa de 12,30 MB a 12,67 MB.

## 3. Un worker

El trabajo es una clase. `CoroutineWorker` es la versión con `suspend`: `doWork()` corre en una corrutina, y si WorkManager para el trabajo (porque se ha perdido la red, o porque se ha cancelado), la corrutina se cancela.

```kotlin
class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val file = inputData.getString("file") ?: return Result.failure()
        return when (upload(file)) {
            Upload.Ok -> Result.success()
            Upload.Busy -> Result.retry()
            Upload.Rejected -> Result.failure()
        }
    }
}
```

Lo que devuelve decide lo que pasa después:

- **`Result.success()`**: hecho. El trabajo acaba en `SUCCEEDED`.
- **`Result.retry()`**: ahora no ha podido, más tarde quizá. Vuelve a la cola con un intento más (`runAttemptCount`) y espera lo que diga el *backoff* (teoría 02).
- **`Result.failure()`**: no tiene arreglo. Acaba en `FAILED`, y lo que va detrás en una cadena, también.

La pregunta para elegir es la misma que en la semana 31 con los reintentos de Ktor: ¿puede salir bien la segunda vez? Sin red o con un 503, sí: `retry`. Con un 400 o una respuesta ilegible, no: `failure`.

`runAttemptCount` empieza en 0. Para rendirse al tercer intento, el que tiene `runAttemptCount == 2`:

```kotlin
Upload.Busy -> if (runAttemptCount + 1 < MAX_TRIES) Result.retry() else Result.failure()
```

## 4. Datos de entrada y de salida

Un trabajo puede esperar días en la cola, y entre tanto el proceso puede morir: lo que necesita no puede vivir en memoria. Se le pasa en un `Data`, que WorkManager guarda con él:

```kotlin
val request = OneTimeWorkRequestBuilder<BackupWorker>()
    .setInputData(workDataOf("file" to "campo.db"))
    .build()

// dentro de doWork
inputData.getString("file")
Result.success(workDataOf("bytes" to 2048L))
```

`Data` solo guarda tipos simples (números, texto, booleanos y sus arrays) y está pensado para poco: un nombre de archivo, un identificador. Lo grande se deja en un archivo o en la base de datos, y el trabajo recibe dónde está. La Bitácora no pasa nada: el `SyncWorker` lee la cola de Room.

> 🔬 Medido: el `Data` de un `success` se lee después en `WorkInfo.outputData`. `Result.retry()` no lleva datos: mientras el trabajo espera el siguiente intento, `outputData` sale vacío (`Data {}`).

## 5. Cómo crea WorkManager un worker

WorkManager guarda en su base de datos el **nombre de la clase**, no el objeto. Cuando le toca, lo crea por reflexión, con un constructor de exactamente dos parámetros: `(Context, WorkerParameters)`. Un worker que necesita algo más (un repositorio, un servidor) necesita una `WorkerFactory`:

```kotlin
class BackupFactory(private val server: BackupServer) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
        if (workerClassName == BackupWorker::class.java.name) BackupWorker(appContext, workerParameters, server) else null
}
```

Devolver `null` le dice a WorkManager «este no es mío»: lo intenta con la reflexión de siempre. La fábrica se le da en su `Configuration`; en la app, como se cuenta en la teoría 04, la da la clase `Application`.

> 🔬 Medido en el ejercicio 01: sin la fábrica en la configuración, WorkManager intenta crear `BackupWorker` por reflexión, no encuentra el constructor de dos parámetros, y el trabajo acaba en `FAILED`. En el test no salta ninguna excepción: solo el estado lo dice.

Con Hilt hay otra forma: `androidx.hilt:hilt-work` (1.4.0) genera la fábrica a partir de `@HiltWorker` y `@AssistedInject`, con un procesador más en KSP. Para un solo worker, la fábrica a mano son cinco líneas y ninguna dependencia nueva: es lo que usa la Bitácora. Con muchos workers, `hilt-work` ahorra escribir una rama por cada uno.

## Para recordar

- Lo que la persona espera delante de la pantalla, en `viewModelScope`. Lo que tiene que acabar aunque cierre la app, en WorkManager.
- WorkManager guarda el trabajo antes de hacerlo: sobrevive a la muerte del proceso y al reinicio. No es inmediato ni exacto.
- `success`, `retry` o `failure`: la pregunta es si puede salir bien la segunda vez. `runAttemptCount` empieza en 0.
- Lo que el trabajo necesita va en un `Data` pequeño, o en la base de datos.
- Un worker con dependencias necesita una `WorkerFactory`; sin ella, el trabajo acaba en `FAILED` sin hacer ruido.

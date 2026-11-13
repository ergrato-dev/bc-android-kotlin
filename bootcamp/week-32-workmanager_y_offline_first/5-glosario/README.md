# Glosario — Semana 32

| Término | Definición |
| --- | --- |
| **APPEND** | Política de trabajo único: el nuevo va detrás del que ya está, en `BLOCKED` hasta que acabe; si el de antes falla, el nuevo también. `APPEND_OR_REPLACE` empieza de cero si el de antes ya falló |
| **AutoMigration** | Migración de Room que se escribe sola comparando dos esquemas exportados. Sirve para añadir tablas y columnas con valor por defecto; renombrar o borrar necesita una especificación |
| **Backoff** | La espera antes de reintentar un trabajo que devolvió `retry()`: `EXPONENTIAL` (se dobla) o `LINEAR` (crece lo mismo). 30 s exponencial por defecto, 10 s como mínimo |
| **BLOCKED** | Estado de un trabajo que espera a que acabe el de antes, en una cadena o con `APPEND` |
| **Cadena** | Trabajos en orden (`beginWith(a).then(b)`): la salida de uno es la entrada del siguiente, y si uno falla, los de detrás también |
| **Cola de cambios** | Tabla con lo que ha cambiado aquí y el servidor aún no tiene. En la Bitácora, `pending`: una fila por código, con la hora y si es un borrado |
| **Condición** (*constraint*) | Lo que el móvil tiene que cumplir para que un trabajo corra: tipo de red, batería, carga, espacio. Si deja de cumplirse mientras corre, el trabajo se para y vuelve a la cola |
| **Configuration.Provider** | Interfaz que implementa la `Application` para darle a WorkManager su configuración (la `WorkerFactory`, el executor). Exige quitar el inicializador automático del manifiesto |
| **CoroutineWorker** | Worker cuyo `doWork()` es `suspend`. Si WorkManager para el trabajo, la corrutina se cancela |
| **CRDT** | Estructura de datos que se puede juntar desde varias copias sin conflictos, porque guarda los cambios en lugar de elegir uno |
| **Data** | Los datos de entrada y salida de un trabajo: tipos simples, poco tamaño. WorkManager los guarda con el trabajo |
| **Doze** | Modo de Android con la pantalla apagada y el móvil quieto: agrupa alarmas, corta la red y aplaza los trabajos a ventanas de mantenimiento |
| **ENQUEUED** | Estado de un trabajo que espera sus condiciones o su backoff |
| **Gana el último** (*last write wins*, LWW) | Regla de conflictos: de dos versiones, la de hora mayor. Converge si todos usan la misma regla y el mismo desempate, y depende de los relojes |
| **Idempotente** | Que hacerlo dos veces da lo mismo que una. `PUT` y `DELETE` lo son; `POST`, no. Lo necesita todo lo que se reintenta |
| **KEEP** | Política de trabajo único: si ya hay uno sin acabar, se queda ese y el nuevo se descarta |
| **Lápida** (*tombstone*) | El recuerdo de un borrado, con su hora, para subirlo y para que una versión más vieja no devuelva lo borrado |
| **Offline-first** | Diseño en el que la app guarda y enseña desde la base de datos local, y la red sincroniza cuando puede. La pantalla nunca espera al servidor |
| **OneTimeWorkRequest** | Petición de un trabajo que corre una vez (más sus reintentos) |
| **PeriodicWorkRequest** | Petición de un trabajo que se repite, cada 15 minutos como mínimo. Nunca acaba en `SUCCEEDED` |
| **Provider** (`javax.inject`) | Algo que da una instancia cuando se le pide (`get()`), no al crearlo. `SyncWorkerFactory` lo usa para no crear el repositorio antes que la configuración de WorkManager |
| **REPLACE** | Política de trabajo único: el que ya está se cancela, aunque esté corriendo, y entra el nuevo |
| **Result** | Lo que devuelve `doWork()`: `success` (hecho), `retry` (más tarde, con backoff) o `failure` (no tiene arreglo) |
| **runAttemptCount** | Cuántas veces ha corrido ya el trabajo: 0 la primera |
| **SynchronousExecutor** | Executor de `work-testing` que lo hace todo en el hilo del test |
| **TestDriver** | En el WorkManager de los tests, lo que dice que se han cumplido las condiciones (`setAllConstraintsMet`), el retraso inicial o el periodo |
| **TestListenableWorkerBuilder** | Crea un worker en un test, con los datos, el intento y la fábrica que digas, para llamar a `doWork()` como una función |
| **Trabajo único** | Trabajo encolado con un nombre (`enqueueUniqueWork`): pedirlo otra vez no hace otro, según la política |
| **WorkerFactory** | Crea los workers por el nombre de su clase. Hace falta cuando un worker necesita algo más que `(Context, WorkerParameters)` |
| **WorkInfo** | El estado de un trabajo: `state`, `runAttemptCount`, `outputData`, `constraints`, `nextScheduleTimeMillis` |
| **WorkManager** | La biblioteca de Android para el trabajo que tiene que acabar aunque se cierre la app o se reinicie el móvil. Guarda cada trabajo en su base de datos y lo programa con `JobScheduler` |
| **WorkManagerInitializer** | El inicializador de `androidx.startup` que arranca WorkManager con la configuración de serie. Se quita del manifiesto para dar la propia |
| **WorkManagerTestInitHelper** | Arranca en los tests un WorkManager donde nada corre hasta que el `TestDriver` lo dice |

# Diseñar y probar un build

> Un build también es código: puede romperse, y lo notas tarde (en el servidor de integración, un viernes). Gradle trae **TestKit** para ejecutar un build dentro de un test y comprobar qué tareas corrieron y con qué resultado.

## 🎯 Objetivos

- Probar un build con `GradleRunner` en una carpeta temporal
- Comprobar resultados de tareas, mensajes y archivos generados
- Aplicar una lista de comprobación a un `build.gradle.kts`

## 1. TestKit

```kotlin
dependencies {
    testImplementation(gradleTestKit())
}
```

```kotlin
val dir = createTempDirectory("sandbox").toFile()
File("sandbox").copyRecursively(dir)                       // un proyecto de ejemplo, copiado

val result = GradleRunner.create()
    .withProjectDir(dir)
    .withArguments("release", "--console=plain")
    .build()                                               // lanza si el build falla

result.task(":stage")?.outcome        // SUCCESS, UP_TO_DATE, FROM_CACHE, SKIPPED, FAILED, NO_SOURCE
result.tasks.map { it.path }          // [:stage, :bundle, :announce, :release] en orden de ejecución
result.output                         // todo lo que Gradle imprimió
```

- **`build()`** espera éxito; **`buildAndFail()`** espera fallo y te deja leer el mensaje.
- **Copia** el proyecto a una carpeta temporal en cada test: así no hay `build/` ni caché de otros tests, y puedes cambiar archivos sin tocar el original.
- Cada `GradleRunner` arranca Gradle de verdad: tarda segundos. Prueba **comportamientos** (resultado, orden, mensaje, archivo), no cada línea.
- Si usas plugins de Kotlin en el proyecto de dentro, apunta TestKit a tu `~/.gradle` (`withTestKitDir`) y no volverá a descargarlos.

## 2. Qué comprobar

| Quieres saber | Cómo lo compruebas |
| --- | --- |
| La tarea hace su trabajo | Lees el archivo de `build/` y comparas con el texto **escrito a mano** |
| Está al día cuando debe | Ejecutas dos veces: `UP_TO_DATE` la segunda |
| Se ejecuta cuando debe | Cambias una entrada: vuelve a `SUCCESS` |
| **No** se ejecuta con lo que no es entrada | Cambias `notes.txt`: sigue `UP_TO_DATE` |
| El orden | `result.tasks.map { it.path }` |
| Una bandera | `-Pquiet`: `SKIPPED` |
| Falla bien | `buildAndFail()` y el mensaje (tarea, propiedad, archivo) |
| Es cacheable | `--build-cache`, `clean`, otra vez: `FROM_CACHE` |
| Es reproducible | Mismo SHA-256 tras `clean` y cambiar una fecha |
| Funciona con la caché de configuración | Dos ejecuciones: `stored` y después `reused` |

## 3. Lista de comprobación de un `build.gradle.kts`

- [ ] Todo con `register`, nunca `create`
- [ ] Las tareas propias tienen `group` y `description`
- [ ] Entradas y salidas declaradas; ninguna ruta `"build/..."` a mano
- [ ] Nada pesado fuera de una acción (se ejecuta en cada comando)
- [ ] Nada de `project` dentro de `doLast` o `@TaskAction`
- [ ] Propiedades leídas con `providers.gradleProperty`; si faltan, un mensaje que dice **cuál** falta y **dónde** ponerla
- [ ] Versiones en un solo sitio (`gradle.properties` o el propio `build`), no repetidas
- [ ] `./gradlew build` pasa dos veces seguidas, y la segunda es casi todo `UP-TO-DATE`

## 4. Para depurar

| Síntoma | Qué hacer |
| --- | --- |
| No sé qué se ejecutará | `--dry-run` |
| Se ejecuta de más | `./gradlew <tarea> --info` explica por qué no estaba al día |
| Se ejecuta de menos | Falta declarar una entrada (lección 3) |
| La caché de configuración se queja | Lee el informe HTML que Gradle enlaza; casi siempre es `project` en una acción |
| Algo es lento | `--scan` (publica un informe en la web de Gradle; más adelante en la fase 2 se usan los *build scans*) |

## ✅ Comprueba

- [ ] ¿Por qué se copia el proyecto de ejemplo a una carpeta temporal en cada test?
- [ ] ¿Qué diferencia hay entre `build()` y `buildAndFail()`?
- [ ] ¿Cómo compruebas que una tarea **no** se ejecuta cuando cambia algo que no es su entrada?

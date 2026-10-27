# Ejercicio 01 — Un plugin con extensión y tarea

⏱️ 2 h · Teoría previa: 01, 02, 03 y 04

## Objetivo

Convertir la tarea `generateBuildInfo` de la semana 9 en un plugin binario, `bitacora.build-info`, que se configura con `buildInfo { domain = "..." }`, toma la versión del proyecto por defecto y, si el proyecto aplica `java`, deja su archivo en los recursos.

## Cómo está montado

`starter/` es un build cuyo producto es el plugin. `BuildInfoExtension.kt` y `GenerateBuildInfo.kt` ya están hechos; tú escribes `BuildInfoPlugin.kt` y su registro en `build.gradle.kts`. Los tests (`src/test/kotlin/PluginTest.kt`, no los toques) son de los dos niveles: dos unitarios con `ProjectBuilder` y cinco funcionales con TestKit, que montan un build de prueba pequeño con la clase `TestBuild` (ya hecha). Los 7 empiezan en rojo.

## Pasos

1. **Registro** (`build.gradle.kts`): `gradlePlugin { plugins { register("buildInfo") { ... } } }` con el id `bitacora.build-info` y la clase `BuildInfoPlugin`.
2. **Extensión** (`BuildInfoPlugin.apply`): `project.extensions.create("buildInfo", BuildInfoExtension::class.java)` y `version.convention(project.provider { project.version.toString() })`.
3. **Tarea**: registra `generateBuildInfo` de tipo `GenerateBuildInfo`, con grupo `bitacora`, una descripción, las entradas conectadas con `set(extension...)` y la salida en `build/generated/build-info`.
4. **Recursos**: dentro de `project.pluginManager.withPlugin("java") { ... }`, añade `task.map { it.outputDir }` a los recursos del source set `main`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia la `convention` por `convention(project.version.toString())` y ejecuta `./gradlew test`. ¿Qué test falla y qué versión aparece en el archivo? ¿Por qué? Restáuralo.
   - (b) Añade `println(extension.domain.get())` dentro de `apply`. Copia el mensaje del test que falla. ¿En qué momento del build se ejecuta `apply`? Quítalo.
   - (c) Quita `@get:Input` de `domain` en `GenerateBuildInfo.kt` y ejecuta `./gradlew validatePlugins`. Copia el problema. ¿Qué le pasaría a `UP-TO-DATE` si Gradle no avisara? Restáuralo.
   - (d) Saca el bloque de los recursos de `withPlugin` y ejecuta `./gradlew test`. ¿Qué test falla y con qué mensaje? Restáuralo.
6. **Ejecuta** `./gradlew build` (7 de 7 y `validatePlugins` en verde).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Plugin with id 'bitacora.build-info' not found` en los tests unitarios | Falta el bloque `gradlePlugin { }`, o el id está mal escrito |
| `Plugin [id: 'bitacora.build-info'] was not found` en los funcionales | Lo mismo: sin registro, `withPluginClasspath()` no tiene nada que ofrecer |
| `version=unspecified` en el archivo | La `convention` lee la versión antes de tiempo: `project.provider { ... }` (paso 2) |
| `Cannot query the value of extension 'buildInfo' property 'domain'` | Hay un `.get()` en `apply`: conecta con `set(...)` en vez de leer |
| `Extension of type 'SourceSetContainer' does not exist` | El bloque de recursos no está dentro de `withPlugin("java")` |
| El test de `UP-TO-DATE` falla en la segunda vuelta | Alguna entrada cambia en cada build: no metas fechas ni valores aleatorios |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.

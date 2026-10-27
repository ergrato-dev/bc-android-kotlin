# Proyecto — La Bitácora con su plugin

⏱️ 4 h · Capa 11 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

El `build-info.properties` de la semana 9 vuelve, esta vez como un **plugin binario** de tu `build-logic`: `bitacora.build-info`, con su extensión `buildInfo { domain = "..." }`. Todo programa de la Bitácora lo recibe a través de `bitacora.kotlin-application`, la versión vive una sola vez en `gradle.properties`, y `app` arranca diciendo qué es: `Bitácora <dominio> v<versión>`. El plugin tiene sus propios tests unitarios, y `./gradlew test` los ejecuta junto con los del build.

## Dónde trabajas

En **tu repositorio**, rama `week-12`, a partir de lo que entregaste en la semana 11. El `starter/` trae la Bitácora de la semana 11 con lo nuevo: en `build-logic`, `BuildInfoExtension.kt` y `GenerateBuildInfo.kt` hechos, `BuildInfoPlugin.kt` vacío y un test unitario de ejemplo; en `app`, un `Main.kt` que lee `build-info.properties`. Copia a tu repositorio los archivos nuevos; tu código de `core`, `data` y `app` no cambia, salvo la primera línea que imprime `app`.

## Pasos

1. **Registro** (`build-logic/build.gradle.kts`): `gradlePlugin { }` con el id `bitacora.build-info` y la clase `BuildInfoPlugin`.
2. **El plugin** (`BuildInfoPlugin.kt`): el del ejercicio 01. Extensión con la versión del proyecto por defecto, tarea conectada con ella y, con `java`, su salida en los recursos.
3. **Tests unitarios** (`build-logic/src/test/kotlin/BuildInfoPluginTest.kt`): al menos 2 más con `ProjectBuilder` (la tarea y su grupo, la versión por defecto...).
4. **Composición** (`bitacora.kotlin-application.gradle.kts`): aplica el plugin por su clase, `pluginManager.apply(BuildInfoPlugin::class.java)`, fuera de `plugins { }`.
5. **Valores**: `version=1.0.0` en `gradle.properties` (sirve para todos los proyectos) y `buildInfo { domain = "<tu dominio>" }` en `app/build.gradle.kts`.
6. **Un solo `test`**: en el `build.gradle.kts` raíz, `tasks.test { dependsOn(gradle.includedBuild("build-logic").task(":test")) }`.
7. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - `:app:run` empieza por `Bitácora <tu dominio> v1.0.0`,
   - el jar de `app` contiene `build-info.properties` con el contenido exacto,
   - cambiar la versión en `gradle.properties` cambia lo que imprime `app` y deja `UP_TO_DATE` los `compileKotlin`,
   - sin `buildInfo { }` en `app`, el build falla con el mensaje de Gradle (`doesn't have a configured value`),
   - `core` y `data` no tienen `generateBuildInfo`: solo los programas llevan build-info,
   - `test` ejecuta `:build-logic:test`,
   - (opcional) `:app:run` con `--configuration-cache`, dos veces: la segunda reutiliza la caché.
8. **Explica en tu README** por qué `bitacora.build-info` es un plugin binario y no otro convention plugin, y por qué se aplica por su clase.
9. **Abre el pull request** `week-12` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, ningún `.get()` en `apply`, valores por defecto perezosos, `withPlugin` para `java`, la versión una vez, tests unitarios del plugin y tests del build con valores calculados a mano.

## Pistas

- `./gradlew -p build-logic test` ejecuta solo los tests del plugin: es el ciclo rápido mientras lo escribes.
- `version=1.0.0` en `gradle.properties` fija `project.version` en **todos** los proyectos del build; por eso la `convention` de la extensión no necesita nada más.
- Con `version` fijada, el jar de `app` se llama `app/build/libs/app-1.0.0.jar`; `java.util.zip.ZipFile` lo abre en un test.
- La versión es una entrada de `generateBuildInfo`, no de `compileKotlin`: por eso cambiarla no recompila.
- `result.task(":build-logic:test")` existe en el resultado de `test` si el paso 6 está bien.
- El mensaje de `:core:generateBuildInfo` empieza por `Cannot locate tasks that match ':core:generateBuildInfo'`.

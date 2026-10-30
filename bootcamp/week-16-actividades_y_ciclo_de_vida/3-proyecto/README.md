# Proyecto — La Bitácora que no pierde nada

⏱️ 4 h · Capa 15 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La primera pantalla de verdad de la Bitácora: un campo para la descripción, un botón por nivel (`LOW`, `MEDIUM`, `HIGH`) y un resumen de lo anotado en la sesión. El borrador y las observaciones aguantan el giro del móvil y la muerte del proceso. Las observaciones se guardan con el JSON de `data`, que ya tienes desde la fase 1. Los tests de la app se ejecutan en la JVM con Robolectric, configurado una vez en `bitacora.android-application`. Su `android-all` pasa por Gradle y por la verificación, como cualquier otra dependencia.

La interfaz es mínima a propósito: vistas creadas en código, sin XML de layouts. Las pantallas de verdad llegan con Compose en la fase 4. Lo que importa esta semana es dónde vive el estado.

## Dónde trabajas

En **tu repositorio**, rama `week-16`, a partir de lo que entregaste en la semana 15. El `starter/` es la Bitácora de la semana 15 resuelta, con comentarios `Paso N` donde va cada cambio. En `src/test/kotlin/BuildTest.kt` hay un test de ejemplo que empieza en rojo: comprueba que `:app:testDebugUnitTest` ejecuta tests. Copia eso a tu repositorio.

## Pasos

1. **El catálogo** (`gradle/libs.versions.toml`): las versiones y bibliotecas del comentario del paso 1, y el bundle `robolectric` = `junit4`, `kotlin-test-junit`, `robolectric`, `androidx-test-core` y `androidx-test-junit`. `android-all` es `org.robolectric:android-all-instrumented`.
2. **Memoria** (`gradle.properties`): `org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8`. Sin esta línea, el paso 7 puede quedarse sin memoria a medias (teoría 05).
3. **Robolectric en el convention plugin** (`bitacora.android-application`):
   - `testOptions`: `unitTests.isIncludeAndroidResources = true` y `--add-opens=java.base/jdk.internal.access=ALL-UNNAMED` en `unitTests.all`.
   - El bundle `robolectric` en `testImplementation`, sacado del catálogo con `findBundle`.
   - Una restricción en `testImplementation` que sube `guava` a la 33.7.2-jre, con `because("GHSA-xxph-c9ww-hj94 ...")` (semana 14, teoría 02). Robolectric 4.17 trae la 33.6.0-jre, afectada por ese aviso (teoría 05).
   - Una configuración `androidAll` con `android-all` y, en las tareas `Test`, `robolectric.offline=true` y `robolectric.dependency.dir` apuntando a la carpeta del jar. Usa `files(...)`, no la `Configuration`, para que la caché de configuración pueda guardarlo.
4. **`app`**: `implementation(libs.activity)` e `implementation(libs.lifecycle.viewmodel.savedstate)`.
5. **El ViewModel** (`app/src/main/kotlin/dev/bitacora/ObservationDraftViewModel.kt`), con un `SavedStateHandle`:
   - `draft`: la descripción que se está escribiendo.
   - `observations`: la lista de la sesión, guardada como el JSON de `encode` y leída con `decode`.
   - `add(level)`: si el borrador no está en blanco, añade una `Observation` con código `A-1`, `A-2`... y vacía el borrador.
6. **La pantalla** (`MainActivity`): `ComponentActivity` y `by viewModels()`. El título es `Bitácora <dominio> v<versión>`, leído de los assets como en la semana 15. Debajo van un `EditText` cuyo texto va al borrador, un botón por nivel y un `TextView` con el resumen por nivel (`LOW: 0 · MEDIUM: 0 · HIGH: 1`). Al añadir, el campo se vacía.
7. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:testDebugUnitTest`. Comprueba que el archivo tiene `robolectric`, `androidx.activity` y `android-all-instrumented`, y que `./gradlew build` sigue en verde con la caché de configuración.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**. Los de Robolectric van en `app/src/test/kotlin/dev/bitacora/`; los del build, en `BuildTest.kt`:
   - el título lleva tu dominio y la versión,
   - añadir numera las observaciones (`A-1`, `A-2`) y vacía el borrador y el campo,
   - un borrador en blanco no añade nada,
   - el borrador, la lista y el resumen sobreviven al giro,
   - y a la muerte del proceso,
   - en `:app`, `dependencyInsight` de `guava` en `debugUnitTestRuntimeClasspath` da la 33.7.2-jre por la restricción, con su motivo,
   - `android-all` alterado en la verificación (`tamper("android-all-instrumented-16-robolectric-13921718-i7.jar")`) hace fallar `:app:testDebugUnitTest`.
9. **Explica en tu README** dónde vive cada dato de la pantalla y por qué, qué pasaría con la lista si la guardaras en una propiedad normal del `ViewModel`, y cuántos componentes tiene ahora la verificación y de dónde salen.
10. **Abre el pull request** `week-16` → `main` con la salida de `./gradlew test` y el número de componentes de la verificación antes y después.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, Robolectric configurado en el convention plugin y no en `app`, `android-all` resuelto por Gradle y verificado, estado en el `SavedStateHandle` y no en la actividad, `core` y `data` sin Android, y tests con valores calculados a mano.

## Pistas

- En un test, lee lo que necesites de la actividad **mientras está viva**, dentro del escenario. Si lees una actividad ya destruida, `by viewModels()` te da un `ViewModel` nuevo y vacío, y el test miente.
- Para simular la muerte del proceso: `Robolectric.buildActivity(MainActivity::class.java).setup()`, cambia lo que quieras, `pause().stop().saveInstanceState(bundle)` y crea otra con `setup(bundle)`. Es lo que hacen los tests del ejercicio 02.
- `setText` en un `EditText` dispara el `TextWatcher`: los tests pueden escribir en el campo como lo haría el usuario.
- Si el paso 7 falla a medias, mira `gradle/verification-metadata.xml` antes de seguir. Un fallo en medio del modo de escritura puede dejarlo vacío; restáuralo con `git checkout` y repite.
- `./gradlew test` ejecuta los tests de Robolectric de `app` (`testDebugUnitTest`), los de `core` y `data` y los de TestKit del raíz. Mientras trabajas en la pantalla, `./gradlew :app:testDebugUnitTest` es mucho más rápido.

# Proyecto — La Bitácora con estrategia de tests

⏱️ 5 h · Capa 33 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora empieza la fase 6 con los mismos tests y una estrategia para ellos:

- **Un solo fake por interfaz.** `FakeRepository` y `FakeSettings` salen de `app/src/test` y pasan a los `testFixtures` de `:core:data`, junto a lo que imitan.
- **Un contrato** que ejecuta los mismos tests contra el fake y contra Room, para que el fake no mienta.
- **Turbine** para los `Flow` del repositorio y del ViewModel.
- **`:core:network` con tests propios**, que hasta ahora no tenía.
- **Kover**, que junta la cobertura de los cuatro módulos `core` y para `check` si baja del 90 %.

```
:app ── testImplementation(testFixtures) ──► :core:data (testFixtures: FakeRepository, FakeSettings)
                                                 │
                    src/test: ObservationRepositoryContract ──► FakeRepository y RoomObservationRepository

raíz ── kover(...) ──► :core:model, :core:database, :core:network, :core:data
```

La app no cambia: el APK es el mismo, y ni los fakes ni Turbine entran en él.

## Dónde trabajas

En **tu repositorio**, rama `week-34`, a partir de lo que entregaste en la semana 33. El `starter/` es la Bitácora de la semana 33 resuelta. Trae ya:

- en `gradle/libs.versions.toml`, Turbine 1.2.1 (`libs.turbine`) y Kover 0.9.11 (`libs.plugins.kover` y, para `build-logic`, `libs.kover.gradle.plugin`);
- en `gradle/verification-metadata.xml`, lo que traen los dos: Turbine, el plugin y el agente de Kover, el generador de informes de IntelliJ y freemarker. Todo revisado en OSV, sin avisos.

Y tests nuevos:

- `src/test/kotlin/TestingTest.kt`, en la raíz, con TestKit: dónde viven los fakes, que no llegan al APK, que el contrato corre contra los dos, que `:core:network` tiene tests, el informe junto de Kover y que la cobertura para el build.

Con la Bitácora de la semana 33 y estos cambios, fallan 5 de los 6 de `TestingTest`; los de `BuildTest`, `ModulesTest` y los de los módulos siguen en verde. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'TestingTest'`. ¿Por qué pasa ya `theFakesAreNotInTheApk`? ¿Qué cambio de este proyecto lo haría fallar si te equivocaras de configuración? (Teoría 03, apartado 4.)
2. **Mide antes de tocar**: copia la tabla de la teoría 01 con tus tiempos (`build/test-results/` después de `./gradlew test`). ¿Qué clase de la app se lleva más tiempo, y por qué esa?
3. **Los fixtures**: `java-test-fixtures` en `:core:data`, y `FakeRepository` y `FakeSettings` a `core/data/src/testFixtures/kotlin/dev/bitacora/data/testing/Fakes.kt`, en el paquete `dev.bitacora.data.testing`. En la app, `testImplementation(testFixtures(project(":core:data")))` y los `import`. Borra `app/src/test/kotlin/dev/bitacora/FakeRepository.kt`. Los tests de la app, en verde sin más cambios.
4. **El contrato**: `core/data/src/test/kotlin/dev/bitacora/data/ObservationRepositoryContract.kt`, con una clase abstracta y dos subclases, `FakeRepositoryTest` y `RoomRepositoryTest` (teoría 02, apartado 4). Al menos 5 tests de lo que la app da por hecho: lo de partida, lo guardado, lo que sale de la lista, el orden por número y un `Flow` que emite cada cambio, con Turbine (teoría 05). Mueve desde `RoomTest` lo que ahora es del contrato y deja allí lo que solo es de Room. Room abre en un `@TempDir` y se cierra en `@AfterEach`.
5. **Lo que encuentra el contrato**: ¿qué test falla con el fake de la semana 33? Arregla el fake, no el test. ¿Por qué no lo veía ningún test de la app?
6. **Turbine en el ViewModel**: en `ViewModelTest`, `refreshingGoesOnAndOff` (`refreshing` pasa de `false` a `true` y vuelve a `false` cuando contesta el repositorio) y `theEffectArrivesOnce` (con `expectNoEvents()`). `testImplementation(libs.turbine)` en la app y en `:core:data`.
7. **La red, sola**: `core/network/src/test/kotlin/dev/bitacora/network/NetworkErrorTest.kt`, con las dependencias de test que necesite su `build.gradle.kts`. Como mínimo: sin red es `OFFLINE`; un `ConnectTimeoutException` es `TIMEOUT` y no `OFFLINE`; un fallo nuestro y la cancelación dan `null`; `observations()` devuelve la lista sin `ObservationsResponse` con `MockEngine`.
8. **Kover**: en `bitacora.kotlin-library`, el plugin (`build-logic` necesita `libs.kover.gradle.plugin`) y los `testFixtures` fuera. En el raíz, `alias(libs.plugins.kover)`, `kover(project(...))` para los cuatro `core`, el filtro de `*_Impl`, la regla `minBound(90)` y la tarea `test` del raíz fuera de la instrumentación (teoría 06).
9. **Lee el informe**: `./gradlew koverHtmlReport` y abre `build/reports/kover/html/index.html`. Después, `./gradlew :core:network:koverLog` antes y después del paso 7. ¿Qué número sale en cada caso, y por qué el del raíz no cambia?
10. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo que no traiga el starter? Después, `./gradlew build`.
11. **Escribe al menos 5 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario. Por ejemplo:
    - en el contrato, guardar una lista vacía deja el repositorio vacío;
    - en el contrato, con Turbine, guardar lo mismo dos veces: ¿cuántas emisiones da el fake, y cuántas Room? Si no coinciden, ¿lo arreglas en el fake o lo dejas fuera del contrato? Explícalo;
    - en `:core:network`, una `HttpRequestTimeoutException` es `TIMEOUT`;
    - en `ViewModelTest`, con `StandardTestDispatcher` en `setMain`, que justo después de `onIntent(Refresh)` el repositorio aún no ha recibido la petición (`refreshes == 0`) y después de `advanceUntilIdle()` sí;
    - en la raíz, que `:core:data:testFixturesJar` no entra en `fieldReleaseRuntimeClasspath` aunque cambies a `debug`.
12. **Explica en tu README** por qué los fakes van en `:core:data` y no en un módulo `:core:testing`; qué encontró el contrato; qué cuenta y qué no cuenta Kover en la Bitácora (pega el resumen de `koverLog`), y por qué la regla es del 90 % de líneas y no de ramas. Dibuja también tu pirámide con los números del paso 2.
13. **Abre el pull request** `week-34` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md):

- un solo `FakeRepository`, en los `testFixtures` de `:core:data`, que no llega al APK;
- el contrato contra los dos, con los mismos tests;
- Turbine en los `Flow`, sin `Channel` ni `collect` a mano;
- `:core:network` con tests propios;
- Kover con el informe de los cuatro `core`, sin código generado ni fakes, y la regla en `check`;
- los tests de la semana 33 en verde;
- sin warnings de Kotlin, `lintFieldDebug` en verde, y tests con los valores esperados escritos a mano.

## Pistas

- `Unresolved reference 'FakeRepository'` en la app: falta el `import dev.bitacora.data.testing.FakeRepository`. Si falla también el `import`, falta `testImplementation(testFixtures(project(":core:data")))`.
- `FakeRepositoryTest > theOrderIsByNumber() FAILED ... but was: <[A-10, A-2, A-1]>`: es lo que tiene que encontrar el paso 5.
- `Plugin [id: 'org.jetbrains.kotlinx.kover'] was not found` en un módulo `core`: `build-logic` no tiene `implementation(libs.kover.gradle.plugin)`.
- `Dependency verification failed` con `kover` o `turbine` en el nombre: tu catálogo no tiene la misma versión que el del starter.
- `koverXmlReport` tarda 10 minutos: está ejecutando los tests de TestKit del raíz. Falta `disabledForTestTasks.add("test")` (paso 8).
- `theReportJoinsTheCoreModules` ve `_Impl`: falta el filtro de lo que genera Room.
- `TurbineAssertionError: Unconsumed events found` en el contrato con Room: Room ha emitido una lista de más. Mira cuál con `awaitItem()` en lugar de `assertEquals`.
- `Dispatchers.Main was accessed when the platform dispatcher was absent`: el ViewModel se crea antes de `setMain` (teoría 04, apartado 5).
- Aviso `Configuration.setVisible(boolean) method has been deprecated`: es de Kover 0.9.11, no tuyo (teoría 06, apartado 1).
- `./gradlew :core:data:test --tests '*RepositoryTest'` y `./gradlew :test --tests 'TestingTest'` para ir test a test.

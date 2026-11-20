# Proyecto — La Bitácora con tests de UI

⏱️ 5 h · Capa 34 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora ya tiene más de 200 tests de pantalla en la JVM. Esta semana se ordenan y se completan:

- **Tags donde no hay texto.** La lista lleva `testTag("observations")`, y la pantalla expone los tags como `resource-id` para UiAutomator y Macrobenchmark (semana 36).
- **Un robot**, `BitacoraRobot`, que esconde cómo se encuentra cada cosa.
- **Espresso-Intents** para comprobar lo que sale al compartir.
- **Capturas con Roborazzi**, comparadas en cada `./gradlew test`, con las referencias en el repositorio.
- **Tests instrumentados** en `src/androidTest`, con Hilt, el orquestador y un emulador gestionado por Gradle. `check` los compila.

```
src/test, src/testField (JVM, Robolectric)                 src/androidTest (dispositivo)
  BitacoraRobot · TagsTest · ShareTest (Intents)             HiltTestRunner · DeviceModule · DeviceTest
  ScreenshotTest ──► src/test/screenshots/*.png              pixel6Api36 (GMD, aosp-atd, API 36)
                       ▲ roborazzi.test.verify=true          check ──► assembleFieldDebugAndroidTest
```

La app cambia en tres sitios: el tag de la lista, `testTagsAsResourceId`, y lo que encuentre la primera captura (paso 7).

## Dónde trabajas

En **tu repositorio**, rama `week-35`, a partir de lo que entregaste en la semana 34. El `starter/` es la Bitácora de la semana 34 resuelta. Trae ya:

- en `gradle/libs.versions.toml`, Roborazzi 1.79.0 (`libs.roborazzi`, `libs.roborazzi.compose`, `libs.plugins.roborazzi`), Espresso 3.7.0 (`libs.espresso.core`, `libs.espresso.intents`), el runner 1.7.0 (`libs.androidx.test.runner`) y el orquestador 1.6.1 (`libs.androidx.test.orchestrator`);
- en `gradle/verification-metadata.xml`, lo que traen: 33 componentes nuevos, todos revisados en OSV, sin avisos.

Y un test nuevo:

- `src/test/kotlin/UiTest.kt`, en la raíz, con TestKit: las capturas en el repositorio, que `test` las compara, que un color cambiado para el build, el test de compartir con Intents, que `check` compila los instrumentados, el emulador gestionado y que nada de esto llega al APK.

Con la Bitácora de la semana 34 y estos cambios, fallan 6 de los 7 de `UiTest`. Los de `BuildTest`, `ModulesTest`, `TestingTest` y los de los módulos siguen en verde. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'UiTest'`. ¿Por qué pasa ya `theTestLibrariesAreNotInTheApk`? ¿Qué error tendría que cometer el paso 6 o el 8 para que fallara?
2. **Mide antes de tocar**: los tiempos de `BitacoraScreenTest`, `MainActivityTest` y `NavigationTest` (`app/build/test-results/testFieldDebugUnitTest/`). Apúntalos: los compararás con los de las capturas.
3. **Los tags**: `Modifier.testTag("observations")` en el `LazyColumn` de `BitacoraScreen`, y `Modifier.semantics { testTagsAsResourceId = true }` en el `NavDisplay` de `MainActivity` (teoría 01, apartado 6). ¿Por qué un tag aquí, y no en los botones?
4. **El robot**: `app/src/test/kotlin/dev/bitacora/BitacoraRobot.kt` con `add`, `share`, `assertRow` y `assertRows`, y la función `bitacora { }` (teoría 02, apartado 5). En `app/src/testField`, `TagsTest` con `theRobotAddsAndFinds` (añade dos y comprueba dos filas) y `theTagsAreResourceIds`. Si `assertRows(2)` encuentra 4, lee la teoría 01, apartado 3.
5. **Compartir con Intents**: `testImplementation(libs.espresso.intents)` y `app/src/testField/kotlin/dev/bitacora/ShareTest.kt` con `shareSendsTheText`: añade una observación con el robot, comparte y comprueba con `intended` el selector y el texto de dentro (teoría 04, apartado 3).
6. **Roborazzi**: el plugin en la app, `roborazzi` y `roborazzi-compose` en `testImplementation`, `outputDir` en `src/test/screenshots` y `roborazzi.test.verify=true` en `gradle.properties` (teoría 06). `app/src/test/kotlin/dev/bitacora/ScreenshotTest.kt` con al menos 4 capturas: las tres filas de nivel, las tres de noche, las tres con letra 1,5 y `BitacoraScreen` con un estado hecho a mano. Sin color dinámico y dentro de `Surface`. Graba con `./gradlew :app:recordRoborazziFieldDebug`.
7. **Lo que encuentra la captura**: abre `dev.bitacora.ScreenshotTest.theScreen.png`. ¿Qué está mal? Arréglalo en la app, no en el test. Comprueba primero que `./gradlew :app:testFieldDebugUnitTest --tests '*ScreenshotTest'` falla con `is changed`, mira el `_compare` y regraba. ¿Por qué no lo había visto ningún test con aserciones?
8. **Los tests instrumentados** (teoría 05):
   - `HiltTestRunner` y `DeviceTest` en `app/src/androidTest/kotlin/dev/bitacora/`, con un módulo que cambie `AppModule` por los fakes de `:core:data`;
   - `testInstrumentationRunner`, `clearPackageData`, `execution = "ANDROIDX_TEST_ORCHESTRATOR"` y el GMD `pixel6Api36` (`Pixel 6`, API 36, `aosp-atd`);
   - las dependencias `androidTestImplementation`, `kspAndroidTest` y `androidTestUtil`.
9. **Que `check` los compile**: `./gradlew :app:build -m | grep AndroidTest` antes y después de hacer que `check` dependa de `assembleFieldDebugAndroidTest`. ¿Qué cambia?
10. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:assembleFieldDebugAndroidTest :app:compileFieldDebugUnitTestKotlin :app:compileDemoDebugUnitTestKotlin` y `git diff --stat gradle/verification-metadata.xml`. ¿Entra algo que no traiga el starter? Si entra `guava-33.6.0-jre`, ¿de dónde viene y cómo se quita? Después, `./gradlew build`.
11. **Escribe al menos 5 tests más**, con lo esperado escrito **a mano** y el porqué en un comentario. Por ejemplo:
    - una captura de `ObservationScreen` (el detalle) y otra con `@Config(qualifiers = "w900dp-h600dp")`;
    - con el robot, borrar una fila deslizando (`performTouchInput { swipeLeft() }`) y comprobar que queda una menos y sale «Deshacer»;
    - con `intending`, la foto de `PlaceAndPhotoTest`: que «Foto» pide `MediaStore.ACTION_IMAGE_CAPTURE` y que con `RESULT_CANCELED` no cambia nada;
    - con el reloj parado, el color de la insignia a mitad de su animación de 300 ms (`advanceTimeBy(150)`): ¿qué esperas, y cómo lo compruebas sin una captura?;
    - en `DeviceTest`, otro recorrido que solo tenga sentido en un dispositivo. Explica por qué.
12. **Explica en tu README** qué encontró la primera captura y por qué ninguna aserción lo veía; cómo se cambia una captura a propósito y cómo se revisa; qué va en la JVM y qué en el dispositivo en tu Bitácora (con tus números del paso 2); y, si tienes una máquina con KVM o un móvil, la salida de `./gradlew pixel6Api36FieldDebugAndroidTest` o `connectedFieldDebugAndroidTest` con su tiempo. Si no, dilo.
13. **Abre el pull request** `week-35` → `main` con la salida de `./gradlew test` y las capturas nuevas a la vista en el diff.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md):

- el tag de la lista y `testTagsAsResourceId`;
- el robot, usado en `TagsTest` y `ShareTest`;
- `intended` sobre el selector y el texto de dentro;
- al menos 4 capturas deterministas, en el repositorio, verificadas por `./gradlew test`;
- el fallo de la captura arreglado en la app;
- `src/androidTest` con Hilt, orquestador y GMD, compilado por `check`;
- los tests de la semana 34 en verde, sin warnings de Kotlin, `lintFieldDebug` en verde y tests con los valores esperados escritos a mano.

## Pistas

- `Expected '2' nodes but found '4' nodes that satisfy: ((TestTag = 'observations').children)`: los hijos de la lista no son las filas (teoría 01, apartado 3).
- `NullPointerException: Intents not initialized`: falta `IntentsRule`, con `order = 0`.
- `Wanted to match 1 intents. Actually matched 0 intents` y en «Recorded intents» sale un `CHOOSER`: el texto va dentro de `EXTRA_INTENT`.
- `captureRoboImage` no deja ningún archivo con `./gradlew test`: falta `roborazzi.test.verify=true`, o lo has puesto en otro `gradle.properties` que no es el del raíz.
- `The original file(...) was not found`: falta grabar, o `outputDir` no apunta a `src/test/screenshots`.
- Las capturas cambian entre dos ejecuciones sin tocar nada: color dinámico, o algo animándose. `dynamicColor = false`, y un estado fijo.
- La captura de noche sale con el fondo negro y la letra de las insignias en negro: falta el `Surface`.
- `Dependency verification failed` con `guava-33.6.0-jre`: `kspAndroidTest` trae el guava del aviso. La misma restricción que `ksp` y `kspTest` (teoría 05, apartado 2).
- `UiTest.thereIsAManagedDevice` falla sin `SKIPPED`: el GMD no se llama `pixel6Api36`, o está en otro bloque que no es `localDevices`.
- `./gradlew pixel6Api36FieldDebugAndroidTest` en WSL2 sin KVM: el emulador no arranca. Es esperado; `UiTest` solo comprueba que la tarea existe.
- El aviso `meta-data#androidx.work.WorkManagerInitializer was tagged ... to remove other declarations but no other declaration present` es de la semana 32, no de esta.

# Ejercicio 02 — Vistas, intents y capturas

⏱️ 1 h 45 min · Teoría previa: 04 y 06

## Objetivo

No toda la UI que vas a probar es de Compose, y no todo lo que hace una pantalla se queda en ella. Al terminar, un parte de campo hecho con vistas tiene sus tests de Espresso. Lo que manda al correo se comprueba con `intended`, la foto de la cámara la contesta `intending`, y una captura de Roborazzi guarda cómo se ve. Todo en la JVM, con Robolectric.

## Cómo está montado

`starter/` es una app de vistas (sin Compose) con Espresso 3.7.0, Espresso-Intents y Roborazzi 1.79.0 ya configurados:

- `app/src/main/res/layout/activity_report.xml`: la nota, «Foto», «Enviar», el estado y la vista previa de la foto. Ya está hecho.
- `app/src/main/kotlin/dev/bitacora/ReportActivity.kt`: la actividad, sin comportamiento. Lo que tiene que hacer está en los `TODO`.
- `app/src/test/kotlin/dev/bitacora/ReportTest.kt`: 6 tests de Espresso y Espresso-Intents.
- `app/src/test/kotlin/dev/bitacora/ScreenshotTest.kt`: una captura, sin la de referencia.
- `gradle.properties`: `roborazzi.test.verify=true`, así que `./gradlew test` compara las capturas.

No toques los tests. Compila, y al empezar fallan 5 de 7.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué pasan ya `typingTurnsSendOn` y `cancellingLeavesNoPhoto`? ¿Qué dice el error de `aReportWithText`?
2. **«Enviar» con texto**: apagado al abrir, y activo solo con texto que no sea solo espacios. Pasan los tres primeros.
3. **El correo**: «Enviar» abre `ACTION_SENDTO` con `mailto:` y la dirección de `REPORTS`, el asunto y la nota sin espacios a los lados. Mira `sendOpensTheMail`: ¿qué comprueba cada matcher de `allOf`?
4. **La foto**: `registerForActivityResult(TakePicturePreview())`. Con foto, la vista previa visible y «Con foto»; sin ella, nada cambia. ¿De dónde sale la foto en `thePhotoComesFromTheCamera`, si Robolectric no tiene cámara? (Teoría 04, apartado 4.)
5. **La captura**: `./gradlew recordRoborazziDebug` y abre `app/src/test/screenshots/dev.bitacora.ScreenshotTest.aReportWithText.png`. ¿Sale lo que esperabas? Después, `./gradlew test`: 7 de 7.
6. **Una captura tuya**: en `ScreenshotTest`, `anEmptyReport`, sin escribir nada. Grábala y comprueba que en la imagen «Enviar» sale apagado. Ejecuta `./gradlew test` y `./gradlew build`.
7. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) En `thePhotoComesFromTheCamera`, quita el `intending(...)`. ¿Qué falla, y por qué no da un error de «no hay cámara»?
   - (b) Cambia «Enviar» por «Mandar» en `strings.xml`. ¿Qué test falla con `./gradlew test`, y con qué mensaje? Ejecuta después `./gradlew compareRoborazziDebug`: ¿falla? ¿Qué hay en `app/build/outputs/roborazzi/`? Abre el `_compare` y descríbelo.
   - (c) En el manifiesto, cambia el tema por `@android:style/Theme.Material.Light` (con barra de acción) y ejecuta `./gradlew test`. ¿Qué enseña el `_compare`? ¿Qué texto tiene la nota en la captura, y por qué? (Teoría 04, apartado 5.)
   - (d) Quita `IntentsRule` de `ReportTest`. Copia la primera línea del error de `sendOpensTheMail`.
   - (e) `ReportTest` no lleva `@GraphicsMode(NATIVE)` y `ScreenshotTest` sí. ¿Por qué? ¿Qué saldría en la captura sin él?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `The original file(...) was not found` | Falta la referencia: `./gradlew recordRoborazziDebug` |
| `... .png is changed` | La pantalla no es la de la referencia: mira `app/build/outputs/roborazzi/*_compare.png` y decide si es un fallo o un cambio querido (teoría 06, apartado 3) |
| `Wanted to match 1 intents. Actually matched 0 intents` | Mira «Recorded intents» en el mensaje: ahí está lo que salió de verdad |
| `hasData` no coincide | La `Uri` tiene que ser exactamente `mailto:campo@bitacora.example`: `"mailto:$REPORTS".toUri()` |
| `onlySpacesIsNoText` falla | `isNullOrBlank()`, no `isNullOrEmpty()` |
| `thePhotoComesFromTheCamera` falla con «Sin foto» | El callback no cambia el estado, o espera la foto en otro sitio: `TakePicturePreview` devuelve el `Bitmap` directamente |
| `Unknown command-line option '--tests'` | `testDebugUnitTest --tests '...'`, no `test` |

## Entrega

Los 7 tests y tu captura en verde, `./gradlew build` en verde, las dos referencias en `src/test/screenshots`, y `NOTAS.md` con las respuestas del paso 7.

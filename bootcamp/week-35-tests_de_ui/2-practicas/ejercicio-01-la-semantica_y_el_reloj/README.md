# Ejercicio 01 — La semántica y el reloj

⏱️ 1 h 45 min · Teoría previa: 01, 02 y 03

## Objetivo

Hay pantallas difíciles de probar por dos motivos. Algunas tienen controles que no dicen qué son, y el test (igual que TalkBack) no los encuentra. Otras cambian con el tiempo, y el test pregunta antes de que hayan cambiado. Al terminar, una valoración dibujada a mano tiene semántica, una nota se guarda sola un segundo después de la última tecla, y sus 11 tests pasan en la JVM sin esperar ese segundo.

## Cómo está montado

`starter/` es una app de Compose con Robolectric, como las de la fase 4, con tres piezas en `app/src/main/kotlin/dev/bitacora/`:

- `Rating.kt`: cinco círculos dibujados con `Canvas`; un toque en el círculo n deja la valoración en n. Sin semántica.
- `NoteField.kt`: un campo de texto con el estado debajo («Guardando…», «Guardado»). Sin la espera ni el guardado.
- `Plots.kt`: una lista que llega de otro hilo. Ya está hecha: es para el paso 5.

`app/src/test/kotlin/dev/bitacora/UiTest.kt` tiene los 11 tests. No los toques, son el enunciado.

Compila, y al empezar fallan 8 de 11.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. ¿Por qué pasan ya `aTapOnTheFourthCircleGivesFour`, `nothingIsSavedBeforeASecond` y `thePlotsArrive`? Para el primero: ¿por dónde llega el toque, si el nodo no tiene acciones? (Teoría 01, apartado 5.)
2. **La semántica de `Rating`**: en el `Modifier` del `Canvas`, un `semantics { }` con `contentDescription`, `stateDescription`, `progressBarRangeInfo` y `setProgress`, como dice el comentario. ¿Por qué `steps = 3` y no 5? Ejecuta `./gradlew testDebugUnitTest --tests '*UiTest'` hasta que pasen los seis tests de la valoración.
3. **El guardado de `NoteField`**: un `LaunchedEffect(text)` con `delay(SAVE_AFTER)`, `onSave(text)` y `saved = text`, sin guardar el texto vacío. ¿Por qué la clave del efecto tiene que ser `text`? ¿Qué pasa con el efecto anterior cuando llega una tecla?
4. Ejecuta `./gradlew test` (11 de 11) y `./gradlew build`.
5. **Lo que llega de otro hilo**: lee `thePlotsArrive`. `load` duerme 300 ms en `Dispatchers.IO`. ¿Por qué el test usa `waitUntil` y no basta con lo que espera cada finder? (Teoría 03, apartados 1 y 2.)
6. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) Añade al final de `itSaysWhereItIs` un `println(rule.onNodeWithTag("rating").printToString())`. Copia lo que sale (está en `app/build/test-results/`). ¿Qué propiedad corresponde a cada línea de tu `semantics { }`?
   - (b) En `type()`, quita `rule.mainClock.advanceTimeByFrame()`. ¿Qué tests fallan, y qué vale `saves`? ¿Por qué no se ha guardado nada a los 1000 ms, si la tecla se pulsó en el 0? (Teoría 03, apartado 3.)
   - (c) En `nothingIsSavedBeforeASecond`, quita `ignoreFrameDuration = true`. ¿Qué falla? ¿Cuánto ha avanzado de verdad el reloj? Calcúlalo a mano y compruébalo con `rule.mainClock.currentTime`.
   - (d) En `itSavesAfterASecond`, quita el segundo `advanceTimeByFrame()`. `saves` ya tiene `"riego"`: ¿por qué no está «Guardado» en la pantalla?
   - (e) En `thePlotsArrive`, cambia el `waitUntil` por `rule.waitForIdle()`. ¿Qué pasa? ¿Cuánto tarda `waitForIdle` (mídelo con `System.nanoTime()`)? ¿Qué cambiarías en `Plots` para no necesitar `waitUntil`? (Teoría 03, apartado 4.)

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unknown command-line option '--tests'` con `./gradlew test` | `test` junta las variantes: usa `testDebugUnitTest --tests '...'` |
| `Failed to perform action SetProgress, the node is missing [SetProgress]` | Falta `setProgress { }` en el `semantics`, o está en otro `Modifier` que no es el del `Canvas` |
| `itIsARangeFromOneToFive` falla con `steps=5` | `steps` cuenta los valores de entre medias: 2, 3 y 4 |
| `talkBackCanChangeIt` da 4 | Redondea con `roundToInt()`: 4,6 es 5 |
| `typingAgainStartsTheWaitAgain` falla y los otros dos del guardado pasan | La clave del `LaunchedEffect` tiene que ser `text`: cada tecla cancela el efecto anterior y lanza otro |
| `ComposeTimeoutException: Condition still not satisfied after 3000 ms` en `withTheClockRunningWaitUntilWaits` | El efecto no llama a `onSave`, o no llega a lanzarse |

## Entrega

Los 11 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 6.

# Ejercicio 02 — Resultados que vuelven

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Abrir una pantalla y recibir lo que devuelve. El nivel de una observación se elige en otra pantalla, con un contrato propio, y la foto la escoge otra app con `GetContent`. El resultado tiene que llegar aunque Android mate la app mientras el usuario elige.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Robolectric. Tiene tres piezas:

- `PickLevel.kt`: el contrato `PickLevel`, con `createIntent` y `parseResult` sin escribir.
- `LevelPickerActivity`: un botón por nivel; falta `choose(level)`.
- `MainActivity`: muestra el nivel (`Level: LOW`) y tiene dos botones, que llaman a `changeLevel()` y `attachPhoto()`. Faltan los lanzadores.

Los tests (`app/src/test/kotlin/dev/bitacora/ResultsTest.kt`, no los toques) prueban el contrato solo, la pantalla que elige con `launchActivityForResult` y la que recibe con `receiveResult` (teoría 05). Los 7 empiezan en rojo.

## Pasos

1. **El contrato** (`PickLevel`):
   - `createIntent`: un Intent explícito a `LevelPickerActivity` con el nombre del nivel actual en `EXTRA_LEVEL`.
   - `parseResult`: el nivel de `EXTRA_LEVEL` si `resultCode` es `RESULT_OK`; si no, o si no hay Intent, `null`.
2. **La pantalla que elige** (`choose`): `setResult(RESULT_OK, ...)` con el nivel en `EXTRA_LEVEL`, y `finish()`. Volver atrás ya devuelve `RESULT_CANCELED` sin que hagas nada.
3. **La pantalla que recibe** (`MainActivity`):
   - Un lanzador como propiedad: `registerForActivityResult(PickLevel()) { ... }`. Si llega un nivel, guárdalo en `level` y llama a `show()`. Si llega `null`, nada.
   - `changeLevel()` lo lanza con el nivel actual.
4. **La foto**: otro lanzador con `ActivityResultContracts.GetContent()`, que guarda la `Uri` en `photo`. `attachPhoto()` lo lanza con `"image/*"`.
5. **Ejecuta** `./gradlew test` (7 de 7).
6. **Lee** `theResultSurvivesProcessDeath`: qué guarda el `Bundle`, de dónde sale el `requestCode` y quién recibe el resultado (teoría 04, diagrama 03).
7. **`NOTAS.md`** (en `starter/`):
   - (a) Mueve el registro de `PickLevel` dentro de `changeLevel()`. ¿Qué tests fallan y con qué excepción? ¿En qué estado dice que está la actividad? ¿Por qué Android exige registrar antes? Restáuralo.
   - (b) Imprime el `requestCode` de dos `changeLevel()` seguidos, el de `attachPhoto()` y el de `changeLevel()` en otra `MainActivity`. ¿Cuáles coinciden? ¿Por qué no son 1, 2, 3?
   - (c) En `theResultSurvivesProcessDeath`, crea la segunda actividad sin el `Bundle` (`setup()`). ¿Qué devuelve `dispatchResult`? ¿Qué significa para el usuario? Restáuralo.
   - (d) Quita `setResult` de `choose` y deja solo `finish()`. ¿Qué test falla y con qué valores? ¿Qué número es `RESULT_OK` y cuál `RESULT_CANCELED`? Restáuralo.
   - (e) Con `shadowOf(app).checkActivities(true)`, llama a `attachPhoto()`. ¿Qué pasa? ¿Cuándo pasaría en un móvil, y cómo lo evitarías?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `LifecycleOwners must call register before they are STARTED` | El registro está dentro de una función que se llama con la actividad ya en marcha. Va como propiedad (teoría 04) |
| `thePickerReturnsTheChosenLevel` da `expected:<-1> but was:<0>` | Falta `setResult(RESULT_OK, ...)` antes de `finish()` |
| `theMainScreenReceivesTheLevel` falla en `Level: LOW` | El callback guarda el nivel pero no llama a `show()` |
| `theContractOnlyAcceptsOk` da un `NullPointerException` | Con `intent` `null`, `parseResult` devuelve `null`: usa `intent?.` |
| `IllegalArgumentException: No enum constant` | El extra lleva `level.name`, y se lee con `Level.valueOf` |
| `nextStartedActivityForResult` es `null` | `changeLevel()` lanza con `startActivity` en lugar de con el lanzador |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.

# Ejercicio 01 — El reducer de la salida

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

La lista de lo que hay que llevar a una salida al campo: agua, el GPS, la libreta. Se añade, se marca, se quita (con deshacer) y se limpian las marcadas. Al terminar, todas las reglas están en una función pura, `reduce`, y se prueban con JUnit sin Android: el proyecto ni siquiera tiene el plugin de Android.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM. `Checklist.kt` trae hechos el estado (`ChecklistState`, con el borrador, las cosas y la última quitada), los intents (`ChecklistIntent`), los efectos (`OfferUndo` y `AllPacked`) y `Next`. `Reducer.kt` tiene `reduce`, que de momento devuelve el estado sin tocar, con comentarios `Paso N`.

Los tests (`src/test/kotlin/dev/bitacora/ReducerTest.kt`, no los toques) prueban cada regla. Al empezar, fallan 6 de 9: los otros 3 piden que no cambie nada, y eso ya lo hace.

## Pasos

1. **`EditDraft`**: el borrador nuevo. Escribe `reduce` como un `when (intent)` usado como valor, **sin `else`** (teoría 02, apartado 3).
2. **`Add`**: el borrador sin espacios, al final de la lista, y el borrador vacío. Sin nombre, o con uno que ya está (sin mirar mayúsculas: `"gps"` y `"GPS"`), devuelve `Next(state)`: el mismo estado, no un `copy()`.
3. **`Toggle`**: marca o desmarca la de ese nombre. Si con este cambio quedan todas marcadas y antes no lo estaban, el efecto `AllPacked`.
4. **`Remove` y `Undo`**: quitar guarda en `removed` cuál era y en qué posición, y pide `OfferUndo(name)`. Deshacer la vuelve a poner en esa posición (o al final, si la lista ya es más corta) y deja `removed` en `null`.
5. **`ClearDone`**: quita las marcadas. Sin ninguna marcada, el mismo estado.
6. Ejecuta `./gradlew test` (9 de 9) y `./gradlew build`.
7. **`NOTAS.md`** (en `starter/`):
   - (a) Añade a `ChecklistIntent` un `data object ClearAll : ChecklistIntent` y compila sin tocar `reduce`. ¿Qué dice el compilador? Copia el mensaje. Ahora pon un `else -> Next(state)` en `reduce`: ¿qué cambia, y por qué es peor? Quita los dos.
   - (b) ¿Por qué `aRepeatedNameIsNotAdded` usa `assertSame` y no `assertEquals`? Escribe un `reduce` que hace que el primero falle y el segundo pase.
   - (c) `AllPacked` es un efecto, no un campo `allPacked: Boolean` del estado. ¿Qué pasaría en un móvil, al girarlo, si fuera un campo?
   - (d) Mira el tiempo de los tests en `build/reports/tests/test/index.html`. ¿Cuánto tarda `ReducerTest`? ¿Qué parte de la Bitácora de la semana 27 se podría probar así y no se hacía?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `'when' expression must be exhaustive` | Falta un intent: añade su caso, no un `else` |
| `anEmptyDraftReturnsTheSameState` falla tras el paso 2 | Devuelves `Next(state.copy())` o una lista nueva igual: devuelve `Next(state)` |
| `theLastOneAsksForAllPacked` falla en la segunda parte | Compruebas solo que ahora están todas marcadas; mira también cómo estaban antes |
| `undoPutsItBackOnce` duplica la cosa | Deshacer no deja `removed` en `null` |
| `IndexOutOfBoundsException` al deshacer | La lista se acortó desde que se quitó: `at.coerceAtMost(size)` |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 7.

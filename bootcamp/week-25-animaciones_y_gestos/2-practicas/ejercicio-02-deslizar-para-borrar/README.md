# Ejercicio 02 — Deslizar para borrar

⏱️ 2 h · Teoría previa: 02, 03 y 04 · Haz antes el ejercicio 01

## Objetivo

Una lista de observaciones que se borran deslizando hacia la izquierda, y solo hacia la izquierda. TalkBack lee cada fila sin el "Borrar" de detrás y la puede borrar con su acción. Al borrar, un aviso "A-2 borrada" ofrece "Deshacer" durante 10 s, y la fila vuelve a su sitio. Las de debajo suben poco a poco.

## Cómo está montado

`starter/` es la app del ejercicio 01 con otra pantalla. Trae hechos `Session` (con `remove`, que devuelve dónde estaba, y `restore`), `MainActivity`, y `SessionScreen`, con la `LazyColumn` (con el código como clave) y el `SnackbarHost` abajo. `DeletableRow` pinta la fila y nada más. Los textos ya están en `strings.xml`.

Los tests (`app/src/test/kotlin/dev/bitacora/DeleteTest.kt`, no los toques) borran con `swipeLeft()` y con la acción de TalkBack, y miran el reloj parado para las filas que suben. Al empezar, fallan los 8.

## Pasos

1. **La fila** (`DeletableRow`): un `SwipeToDismissBox` con `enableDismissFromStartToEnd = false` y `onDismiss = { onDelete() }`. El estado, con `remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, threshold) }` y `threshold = SwipeToDismissBoxDefaults.positionalThreshold` leído fuera del `remember` (teoría 04, apartado 4). Detrás, "Borrar" con `Modifier.clearAndSetSemantics {}`. La fila, con `background(MaterialTheme.colorScheme.surface)`.
2. **TalkBack**: en el `modifier` del `SwipeToDismissBox`, `semantics(mergeDescendants = true) { customActions = listOf(CustomAccessibilityAction(delete) { onDelete(); true }) }`.
3. **Deshacer** (`SessionScreen`): con `rememberCoroutineScope()`, al borrar, `session.remove(observation)` y, en una corrutina, `snackbar.showSnackbar(...)` con `R.string.deleted`, `actionLabel` con `R.string.undo` y `SnackbarDuration.Long`. Si devuelve `SnackbarResult.ActionPerformed`, `session.restore(observation, index)`. Los textos, con `LocalResources.current`.
4. **Las de debajo**: `Modifier.animateItem()` en cada fila (teoría 02). Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia el estado por `rememberSwipeToDismissBoxState()`. ¿Qué test falla? Pon un `println` en `remove` y `restore`: ¿qué se imprime al deshacer, y por qué? Restáuralo.
   - (b) Quita `enableDismissFromStartToEnd = false`. ¿Qué test falla? ¿Por qué un gesto con dos sentidos que hacen lo mismo es un problema? Restáuralo.
   - (c) Quita el `clearAndSetSemantics {}` del fondo. ¿Qué test falla, y qué leería TalkBack en cada fila? Restáuralo.
   - (d) Quita `duration = SnackbarDuration.Long`. ¿Qué test falla? ¿Cuánto dura el aviso, y por qué? Restáuralo.
   - (e) Quita `animateItem()`. ¿Qué test falla, con qué números? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `@Composable invocations can only happen from the context of a @Composable function` en el `remember` | `SwipeToDismissBoxDefaults.positionalThreshold` es composable: léelo en una variable antes del `remember` |
| `undoPutsItBackInItsPlace` no encuentra A-2 | El estado se guarda: la fila vuelve deslizada y se borra otra vez (paso 5a) |
| `talkBackCanDeleteToo`: `Expected exactly '1' node` o la acción no está | `customActions` va en el nodo que une la fila (`mergeDescendants = true`) |
| `theSnackbarGoesAwayAfterTenSeconds` sigue viendo "Deshacer" | Falta `SnackbarDuration.Long`: con una acción, por defecto no se va |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.

# Ejercicio 02 — Una lista larga

⏱️ 2 h · Teoría previa: 03, 04 y 05 · Haz antes el ejercicio 01

## Objetivo

Una sesión de campo de 500 observaciones en una lista que solo compone las que se ven, agrupadas por nivel con una cabecera fija. Cada fila se abre al pulsarla y sigue abierta aunque lleguen otras delante, al girar el móvil y tras la muerte del proceso. Un botón "Arriba" aparece cuando el usuario ha bajado, y lo devuelve al principio.

## Cómo está montado

`starter/` es la app del ejercicio 01. `ObservationRow.kt` trae ya hecha la fila (sin la casilla), que enseña el código y la ubicación solo si está abierta (`expanded`). `Observation.kt` trae `longSession`: de la A-1 a la A-500, con los niveles alternados y ubicación en las pares. `MainActivity` pinta `ObservationList(longSession)`; `ObservationList` está vacía.

Los tests (`app/src/test/kotlin/dev/bitacora/ObservationListTest.kt`, no los toques) prueban la lista sola con `createComposeRule`, y la actividad entera con `createEmptyComposeRule` para la muerte del proceso. Al empezar, los 8 fallan.

## Pasos

1. **La lista**: un `LazyColumn` que ocupa todo, con las observaciones agrupadas por nivel (`groupBy`), de Alta a Baja. Cada grupo con `items(group, key = { it.code }, contentType = { "observation" })`.
2. **Las cabeceras**: antes de cada grupo, `stickyHeader(key = level.name)` con `"Alta · 166"` (`R.string.header`), con fondo para que las filas no se vean a través y un margen de 16 dp. Los niveles sin observaciones no tienen cabecera.
3. **Las filas**: cada una con `var expanded by rememberSaveable { mutableStateOf(false) }` dentro del `items`, y `ObservationRow(observation, expanded, { expanded = !expanded })`.
4. **"Arriba"**:
   - `val state = rememberLazyListState()`, que se pasa al `LazyColumn`;
   - `val scrolled by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }`;
   - la lista y el botón en un `Box`; con `scrolled`, un `Button` "Arriba" (`R.string.up`) abajo a la derecha (`Modifier.align(Alignment.BottomEnd)`), que hace `scope.launch { state.animateScrollToItem(0) }` con `val scope = rememberCoroutineScope()`.
5. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la `key` de las filas. ¿Qué tests fallan, y con qué mensajes? ¿Cuál de ellos es un fallo que vería el usuario, y cuáles son del test? Restáuralo.
   - (b) Cambia el `rememberSaveable` de `expanded` por `remember`. ¿Qué test falla? ¿Qué más perdería el usuario, sin girar el móvil? (Pista: abre una fila, baja hasta que salga de la pantalla y vuelve.) Restáuralo.
   - (c) Cambia el `derivedStateOf` por `if (state.firstVisibleItemIndex > 0)`. ¿Falla algún test? ¿Qué dice `./gradlew :app:lintDebug`? ¿Qué cuesta, según la teoría 05? Restáuralo.
   - (d) Usa la observación entera de clave: `key = { it }`. ¿Qué excepción da, y por qué? Restáuralo.
   - (e) Cambia `stickyHeader` por `item`. ¿Qué test falla? Restáuralo.
   - (f) Cambia el `LazyColumn` por una `Column` con `verticalScroll` y un `for`. ¿Cuántos nodos hay ahora en el árbol de semántica? (`rule.onAllNodesWithText("observación", substring = true).fetchSemanticsNodes().size` en un test tuyo.) Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Failed to scroll to the item identified by "A-300", couldn't find the key.` | Las filas no tienen `key` (teoría 03) |
| `Type of the key ... is not supported` | La clave tiene que caber en un `Bundle`: el código, no la observación (teoría 04) |
| `Key "..." was already used` | Una cabecera y una fila, o dos cabeceras, con la misma clave |
| `theHeadersGroupByLevel` falla con el orden cambiado | Ordena los grupos de Alta a Baja (`compareByDescending`), no por orden de llegada |
| `anOpenRowSurvives` falla: la fila no se abre | La cabecera fija tapa la fila de arriba y el toque cae en ella. Con un `Surface` de Material, la cabecera se queda el toque; con `Modifier.background`, lo deja pasar a la fila |
| `theScrollSurvives` falla | ¿El `LazyColumn` recibe el `state` de `rememberLazyListState`? |
| `Unresolved reference 'launch'` | `import kotlinx.coroutines.launch` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.

# Ejercicio 01 — La sesión que se adapta

⏱️ 2 h · Teoría previa: 01 y 03

## Objetivo

Una sesión con tres observaciones que siempre es un panel: la lista, y al tocar una, su detalle en su lugar. Al terminar, en un móvil sigue así (y atrás vuelve a la lista), con 840 dp o más la lista y el detalle van uno al lado del otro, medio plegada sobre la mesa el detalle va arriba de la bisagra y la lista abajo, y la elegida sobrevive a desplegar el móvil.

## Cómo está montado

`starter/` trae hechos `Observation` (las tres de la sesión), `MainActivity`, el tema y los textos, y en el build `adaptive` 1.3.0 y, para los tests, `window-testing` 1.5.1. `SessionScreen.kt` tiene el enum `Layout`, la función `layoutFor` (que siempre dice `ONE_PANE`), la pantalla y sus dos piezas, `ObservationList` y `Detail`, con comentarios `Paso N`.

Los tests (`app/src/test/kotlin/dev/bitacora/`, no los toques): `LayoutForTest` prueba la decisión sin pantalla; `SessionScreenTest`, la pantalla a 320, 900 y 600 x 800 dp con una bisagra de mentira; `UnfoldTest`, el cambio de 320 a 900 dp. Al empezar, fallan 6 de 9.

## Pasos

1. **La decisión** (`layoutFor`): `TABLETOP` si `posture.isTabletop`; si no, `TWO_PANES` si `sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)`; si no, `ONE_PANE` (teoría 01, apartado 2).
2. **La ventana**: al principio de `SessionScreen`, `val info = currentWindowAdaptiveInfoV2()` (sin el `V2`, no compila).
3. **La forma**: un `when (layoutFor(info.windowSizeClass, info.windowPosture))`. `ONE_PANE`, lo que hay ahora. `TWO_PANES`, una `Row` con la lista de `360.dp` y el detalle con `weight(1f)`. `TABLETOP`, una `Column` con el detalle de alto `hinge.top` y la lista debajo, con `padding(top = hinge.height)`: `val hinge = info.windowPosture.allHorizontalHingeBounds.first()`, en píxeles, pasados a dp con `LocalDensity` (teoría 03, apartado 2).
4. **Atrás**: en `ONE_PANE`, `BackHandler(enabled = observation != null) { selected = null }`.
5. **La elegida**: `rememberSaveable` en vez de `remember`.
6. Ejecuta `./gradlew test` (9 de 9) y `./gradlew build`.
7. **`NOTAS.md`** (en `starter/`):
   - (a) En `layoutFor`, pon la comprobación del ancho antes que la de la mesa. ¿Qué test falla, con qué mensaje? ¿Por qué `tabletopPutsTheDetailAboveTheHinge` sigue en verde? Restáuralo.
   - (b) Vuelve a `remember`. ¿Qué test falla? Tras recrear a 900 dp, ¿qué se ve a cada lado? ¿Por qué? Restáuralo.
   - (c) Quita el `BackHandler`. ¿Qué test falla, y qué le pasa a la actividad? Restáuralo.
   - (d) ¿Por qué `SessionScreenTest` pinta la pantalla con `setContent` desde el test, y no con `MainActivity` como `UnfoldTest`?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `'fun currentWindowAdaptiveInfo(...)' is deprecated` | Con `allWarningsAsErrors`, la deprecación no compila: `currentWindowAdaptiveInfoV2()` |
| `Unresolved reference 'hinges'` | En adaptive 1.3.0 es `hingeList`; para los `Rect`, `allHorizontalHingeBounds` |
| `expandedShowsBoth`: `... y ...` con el detalle a la izquierda de 360 | La lista necesita `width(360.dp)` y el detalle `weight(1f)`, dentro de la misma `Row` |
| `tabletopPutsTheDetailAboveTheHinge` con el detalle de 800 dp | La altura de la bisagra viene en píxeles: `toDp()` dentro de `with(LocalDensity.current)` |
| `unfoldingKeepsTheSelection` no encuentra la descripción | La elegida se pierde al recrear: `rememberSaveable` |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 7.

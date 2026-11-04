# Ejercicio 02 — La lista que no recompone

⏱️ 2 h · Teoría previa: 04 y 05 · Haz antes el ejercicio 01

## Objetivo

Una sesión de cien observaciones con un borrador arriba. Se ve bien y funciona, pero cada tecla recompone todas las filas que se ven, cambiar el nivel de una recompone las demás, y al bajar la pantalla entera se recompone con cada fila que sale por arriba. Al terminar, cada tecla no recompone ninguna fila, un nivel nuevo solo la suya, y bajar recompone la pantalla una vez, cuando aparece "Arriba".

## Cómo está montado

`starter/` trae hechos `Observation` y `Session` (cien de ejemplo, el borrador, añadir y cambiar el nivel), `MainActivity`, y `Recompositions`, un contador para aprender (teoría 05, apartado 1). `SessionScreen.kt` tiene la pantalla, `Item` (lo que pinta una fila) y `ObservationRow`, con comentarios `Paso N`. `app/build.gradle.kts` ya saca los informes del compilador de Compose a `app/build/compose`.

Los tests (`app/src/test/kotlin/dev/bitacora/RecompositionTest.kt`, no los toques) cuentan composiciones después de empezar. Al empezar, fallan 4 de 8: los otros 4 comprueban que la pantalla sigue haciendo lo mismo.

## Pasos

0. **Antes de tocar nada**, `./gradlew :app:compileDebugKotlin` y busca `ObservationRow` en `app/build/compose/app-composables.txt` y `Item` en `app-classes.txt`. Apunta qué dicen.
1. **`Item`**: márcala `@Immutable` (teoría 04, apartado 3).
2. **"Arriba"**: `val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }` (teoría 05, apartado 4).
3. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
4. **`NOTAS.md`** (en `starter/`):
   - (a) Vuelve a ejecutar `./gradlew :app:compileDebugKotlin --rerun` y compara el informe con el del paso 0. ¿Qué cambia en `Item` y en el parámetro de `ObservationRow`? ¿Por qué, tras `./gradlew test`, `app-composables.txt` no tiene `ObservationRow`?
   - (b) Quita `@Immutable` y pon el `map` en `remember(observations) { ... }`. ¿Qué test falla, con qué números? ¿Por qué teclear ya no recompone y cambiar un nivel sí? Restáuralo.
   - (c) Quita `derivedStateOf` (vuelve a `list.firstVisibleItemIndex > 0`). ¿Qué test falla, con qué número? ¿De dónde salen esas recomposiciones? Restáuralo.
   - (d) En el móvil, ¿con qué herramienta verías estas cuentas sin el contador?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `typingDoesNotRecomposeTheRows`: `[4, 4, 4, 4]` | El `map` saca `Item` nuevos con cada recomposición, e `Item` no es estable: `@Immutable` |
| `aNewLevelRecomposesOnlyItsRow`: `[1, 1, 1, 1]` | La lista es nueva y sus `Item` también: sin `@Immutable` se comparan con `===` |
| `scrollingRecomposesTheScreenOnce`: `expected:<1> but was:<8>` | El índice se lee en la composición: `derivedStateOf` dentro de un `remember` |
| El informe no tiene `ObservationRow` | Lo ha sobrescrito la compilación de los tests: `./gradlew :app:compileDebugKotlin --rerun` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 4.

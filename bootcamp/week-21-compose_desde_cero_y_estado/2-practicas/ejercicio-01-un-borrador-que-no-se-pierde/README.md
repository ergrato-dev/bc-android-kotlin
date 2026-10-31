# Ejercicio 01 — Un borrador que no se pierde

⏱️ 2 h · Teoría previa: 01, 02, 03 y 04

## Objetivo

La pantalla del borrador de una observación, en Compose: un campo de texto con su contador, un selector de nivel y un botón para guardar. El selector no tiene estado: pinta lo que le dicen. La pantalla sí lo tiene, y no lo pierde ni al girar el móvil ni cuando Android mata el proceso.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Compose ya configurado en el build y Robolectric para los tests. `MainActivity` solo hace `setContent { DraftScreen() }`. `Draft.kt` tiene los dos composables vacíos, `LevelPicker` y `DraftScreen`. Los textos están en `res/values/strings.xml` y los niveles, en `Level.kt`.

Los tests (`app/src/test/kotlin/dev/bitacora/DraftTest.kt`, no los toques) pintan cada composable con `createComposeRule` y buscan los nodos por su texto. Al empezar, los 9 fallan: los composables no pintan nada.

## Pasos

1. **`LevelPicker`, sin estado**:
   - una `Column` con una `Row` por nivel (`Level.entries`), con un `RadioButton` y el texto del nivel (`stringResource(level.label)`);
   - la fila es la que se pulsa: `Modifier.selectable(selected = ..., onClick = { onSelect(level) }, role = Role.RadioButton)`, y el `RadioButton` lleva `onClick = null`;
   - ningún `remember`: la fila marcada es la de `selected`.
2. **El estado de `DraftScreen`**: el texto (`String`), el nivel (`Level?`) y cuántas se han guardado (`Int`), cada uno con `var ... by rememberSaveable { mutableStateOf(...) }` (`mutableIntStateOf` para el número).
3. **Lo que pinta `DraftScreen`**, en una `Column`:
   - un `TextField` con la etiqueta "Descripción" que no deja pasar de `MAX_LENGTH` caracteres (`it.take(MAX_LENGTH)`);
   - el contador, `"5/80"` (`R.string.characters`);
   - `LevelPicker`, con el nivel del estado y una lambda que lo cambia;
   - el botón "Guardar", activo solo con texto (que no sea solo espacios) y nivel. Al pulsarlo, suma una y vacía el texto y el nivel;
   - `"Guardadas: N"` (`R.string.saved`).
4. **Ejecuta** `./gradlew test` (9 de 9) y `./gradlew build`, que pasa también lint.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia el `rememberSaveable` del texto por `remember`. ¿Qué tests fallan? ¿Qué le pasaría al usuario, y cuándo? Restáuralo.
   - (b) Quita el `remember` del nivel: `var level by mutableStateOf<Level?>(null)`. ¿Qué tests fallan? ¿Qué dice `./gradlew :app:lintDebug`? ¿Por qué el nivel no se queda marcado al pulsarlo? Restáuralo.
   - (c) Junta el texto y el nivel en una `data class Draft(val text: String, val level: Level?)` y guárdala con un solo `rememberSaveable`. ¿Qué excepción da, y en qué tests? ¿Qué harías para guardarla? Restáuralo.
   - (d) Dale a `LevelPicker` su propio estado: `var current by remember { mutableStateOf(selected) }`, que cambia al pulsar y es el que se pinta. ¿Qué tests fallan? ¿Por qué falla `savingCountsAndClearsTheDraft`, si la pantalla vacía el nivel? Restáuralo.
   - (e) Comenta `debugImplementation(libs.compose.ui.test.manifest)` en `app/build.gradle.kts`. ¿Qué tests fallan, con qué mensaje, y cuál no? ¿Por qué? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'selectable'` | `import androidx.compose.foundation.selection.selectable` |
| `assertIsSelected` falla con `Failed to assert the following: (Selected = 'true')` | El `selectable` va en la `Row` que tiene el texto, no en el `RadioButton` |
| `Property delegate must have a 'getValue(Nothing?, KMutableProperty0<*>)' method` | Faltan `import androidx.compose.runtime.getValue` y `setValue` |
| `Error: Creating a state object during composition without using remember` | Un `mutableStateOf` sin `remember` ni `rememberSaveable` (teoría 02) |
| `MutableState containing ... cannot be saved using the current SaveableStateRegistry` | `rememberSaveable` solo guarda lo que cabe en un `Bundle` (teoría 03) |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.

# Ejercicio 02 — El formulario de una observación

⏱️ 2 h · Teoría previa: 04 y 05 · Haz antes el ejercicio 01

## Objetivo

El formulario para dar de alta una observación: un título, una descripción que avisa cuando es demasiado larga, el nivel elegido entre tres, y "Guardar", que solo se puede pulsar cuando hay algo que guardar. También desde el teclado, con su botón "Hecho".

## Cómo está montado

`starter/` es la app del ejercicio 01, con su tema resuelto. `MainActivity` guarda el borrador y el nivel con `rememberSaveable` y pinta `ObservationForm`; al guardar, enseña la observación en un `Toast` y vacía el formulario. En `ObservationForm.kt` está `MAX_DESCRIPTION` (80) y el composable vacío. Los textos ya están en `strings.xml`.

Los tests (`app/src/test/kotlin/dev/bitacora/ObservationFormTest.kt`, no los toques) pintan el formulario con su estado en el test. Al empezar, los 8 fallan.

## Pasos

1. **La columna y el título**: una `Column` con `modifier`, 16 dp de margen y `Arrangement.spacedBy(16.dp)`. El título, `R.string.new_observation`, con `style = MaterialTheme.typography.titleLarge`.
2. **El campo**: un `OutlinedTextField` a todo el ancho y de una línea (`singleLine`), con `label` ("Descripción") y `supportingText` con la cuenta (`R.string.counter`: `"5 / 80"`). En error (`isError`) con más de `MAX_DESCRIPTION`. `keyboardOptions` con `KeyboardCapitalization.Sentences` e `ImeAction.Done`, y `keyboardActions` con un `onDone` que guarda **solo si se puede**.
3. **El nivel**: una `SingleChoiceSegmentedButtonRow` a todo el ancho, con un `SegmentedButton` por nivel (`selected`, `onClick` y `shape = SegmentedButtonDefaults.itemShape(index, Level.entries.size)`) y su texto (`label`).
4. **"Guardar"**: un `Button` con `enabled` solo si la descripción no está en blanco, no se pasa del máximo y hay nivel. Calcula esa condición una vez (`val canSave = ...`) y úsala aquí y en el `onDone`.
5. **Ejecuta** `./gradlew test` (8 de 8) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita el `if (canSave)` del `onDone`. ¿Qué test falla, con qué mensaje? ¿Qué podría guardar el usuario sin ver el botón apagado? Restáuralo.
   - (b) Cambia `OutlinedTextField` por `TextField`. ¿Falla algún test? ¿Qué cambia para el usuario? ¿Qué te dice eso de lo que comprueban los tests? Restáuralo.
   - (c) Cambia la fila de botones segmentados por una `Row` con un `FilterChip` por nivel. ¿Qué test falla, y qué `Role` encuentra? ¿Por qué importa para TalkBack? Restáuralo.
   - (d) Escribe un test tuyo con `@Config(qualifiers = "es")` que compruebe el texto del error en la semántica (`SemanticsMatcher.expectValue(SemanticsProperties.Error, ...)`). ¿Qué texto es? ¿Y sin el calificador?
   - (e) Escribe un test tuyo con `@Config(fontScale = 2f)` que imprima el alto del campo (`getUnclippedBoundsInRoot()`). Compáralo con el de la escala 1. ¿Por qué no crece al doble (teoría 04)?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `This material API is experimental` | Has usado un componente experimental (`TopAppBar`...); este ejercicio no lo necesita |
| `theCounterCounts` no encuentra `5 / 80` | La cuenta va en `supportingText`, con `draft.length` y `MAX_DESCRIPTION` |
| `tooLongIsAnError`: `80 / 80` ya está en error | Es `>`, no `>=`: 80 caracteres valen |
| `theLevelsAreOneChoice`: `Role = 'Button'` | Son `SegmentedButton` dentro de una `SingleChoiceSegmentedButtonRow`, no `Button` |
| `doneOnTheKeyboardSaves` no guarda | Falta `imeAction = ImeAction.Done` o el `keyboardActions` |
| `theTitleIsATitle` da 16 sp | Falta `style = MaterialTheme.typography.titleLarge`: sin estilo, un `Text` es `bodyLarge` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.

# Ejercicio 01 — La fila de una observación

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

La fila con que la Bitácora enseña una observación en una lista: a la izquierda, un cuadrado con la letra de su nivel; en medio, la descripción y, debajo, el código y la ubicación; a la derecha, una casilla para marcarla como revisada. La fila ocupa todo el ancho, se pulsa entera, y una descripción larga no saca la casilla de la pantalla.

## Cómo está montado

`starter/` es una app Android de un solo módulo con Compose y Robolectric, como en la semana 21. `MainActivity` pinta las tres observaciones de ejemplo (`samples`, en `Observation.kt`) con `ObservationRow`. `Level.kt` tiene el texto y la letra de cada nivel. En `ObservationRow.kt` están vacíos `LevelBadge` y `ObservationRow`.

Los tests (`app/src/test/kotlin/dev/bitacora/ObservationRowTest.kt`, no los toques) pintan cada composable con `createComposeRule` y comprueban dónde queda cada cosa, en dp, con las cuentas en un comentario. Al empezar, los 9 fallan.

## Pasos

1. **`LevelBadge`**: un `Box` de 40 dp (`size`), con el fondo del color del nivel (`background`; un `Color` por nivel, por ejemplo con un `when` en una propiedad privada `Level.color`) y su letra en el centro (`contentAlignment = Alignment.Center`, `stringResource(level.short)`). La cadena empieza por el `modifier` que recibe.
2. **`ObservationRow`**: una `Row` con `modifier`, que ocupa todo el ancho, se pulsa entera (`clickable(onClick = onClick)`) y lleva un margen de 16 dp **dentro** de la zona que se pulsa, con los hijos centrados en vertical. Dentro:
   - la insignia;
   - un `Spacer` de 16 dp de ancho;
   - una `Column` con la descripción y, debajo, `"A-1 · -34.6037, -58.3816"` (`R.string.code_and_place`), o `"A-1 · Sin ubicación"` sin ubicación;
   - un `Checkbox` con `checked` y `onCheckedChange`.
3. **El reparto**: la `Column` del texto se queda con lo que sobra (`Modifier.weight(1f)`), para que una descripción larga no empuje la casilla fuera.
4. **Ejecuta** `./gradlew test` (9 de 9) y `./gradlew build`, que pasa también lint.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia el orden a `.padding(16.dp).clickable(...)`. ¿Qué test falla, con qué mensaje? ¿Qué notaría el usuario? Restáuralo.
   - (b) Quita el `weight(1f)` de la `Column`. ¿Qué tests fallan? ¿Dónde queda la casilla con una descripción corta, y con una larga? ¿Por qué? Restáuralo.
   - (c) Escribe la insignia como `Box(Modifier.size(40.dp).then(modifier)...)`. ¿Qué test falla, y por qué mide lo que mide? Restáuralo.
   - (d) Renombra el parámetro `modifier` de `LevelBadge` a `mod`. ¿Qué dice `./gradlew :app:lintDebug`? ¿Para el build? Restáuralo.
   - (e) Quita el `Spacer`. ¿Qué test falla? Haz la cuenta de dónde empieza ahora el texto. Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'weight'` | `weight` solo existe dentro de una `Row` o una `Column`: se escribe en un hijo, no en la `Row` |
| `Failed to retrieve bounds of the node ... Testtag = 'row'` | La `Row` no usa el `modifier` que le pasa el test |
| `theTextsStartAfterTheBadge` da 56 o 88 | Revisa el `Spacer` y si el margen está en la `Row` o en sus hijos |
| `theCheckboxIsAtTheEnd` da 113 | Falta el `weight` (teoría 01) |
| Un test de posición de texto da un ancho raro | Robolectric mide 1 dp por carácter: compara dónde empieza el texto, no cuánto mide (teoría 01) |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 5.

# Proyecto — `Observation` y `Logbook`

⏱️ 4 h · Capa 3 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La **Bitácora como objeto**. `Observation` deja de validarse con una función aparte: se valida **ella misma** al crearse. `Logbook` guarda las observaciones, no deja que nadie las toque por fuera, cuenta por nivel y escribe su informe.

## Dónde trabajas

En **tu repositorio**, rama `week-04`. El `starter/` trae `Base.kt` (con `level`, `header`, `format2`, `Stats` y `statistics`) y la interfaz `Reportable` para que compile solo; **usa tus versiones de las semanas anteriores y tus nombres del dominio**.

## Pasos

1. **`Observation`**: el `init` valida las cuatro reglas, cada una con `require` y un mensaje que incluya el valor que llegó. Implementa `compareTo` (por `measure` y, en empate, por `code`).
2. **`Observation.parse`** (en el `companion object`): una línea `CODE;descripción;cantidad;medida` → `Observation?`. Devuelve `null` si no tiene 4 partes, si cantidad o medida no son números o si el `init` la rechaza. Captura `IllegalArgumentException`, no `Exception`. Esta función **sustituye** a la `parseObservation` y a la `validated` de la semana 2: bórralas.
3. **`Observation.level`**: propiedad de extensión que usa la función `level` de `Base.kt`.
4. **`Logbook`**:
   - `observations` devuelve una **copia** de solo lectura; `size` es calculada.
   - `add` devuelve `false` si ya hay una observación igual (la `data class` ya compara por valor), `true` si la añade.
   - `remove(code)` borra todas las de ese código y devuelve cuántas.
   - `countByLevel()` siempre incluye `LOW`, `MEDIUM` y `HIGH`.
   - `report()` implementa `Reportable`: cabecera, una línea `code | description | quantity | measure con 2 decimales | level` por observación (en orden de inserción) y `Total: N`.
   - `Logbook.fromLines(domain, lines)` en el `companion object` usa `Observation.parse` y descarta lo inválido.
5. **Escribe al menos 8 tests** en `LogbookTest.kt`, calculados **a mano**:
   - una por cada regla del `init`, con `assertFailsWith`,
   - `parse` con una línea válida y con cada tipo de línea inválida,
   - el orden natural con `sorted()` y un empate en `measure`,
   - `level` como propiedad en los dos extremos,
   - `add` de un duplicado, `remove` con conteo (y con un código que no existe),
   - que `observations` es una copia (guárdala, añade, comprueba que no cambió),
   - el texto exacto de `report` y `fromLines` con una línea inválida.
6. **Cambia `Main.kt`** por datos de tu dominio y ejecuta `./gradlew run`.
7. **Abre el pull request** `week-04` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, sin exponer la lista mutable, invariantes en `init`, tests propios con valores calculados a mano.

## Pistas

- `data class` con `init`: el bloque `init` va dentro de las llaves de la clase, después del constructor.
- `compareValuesBy(this, other, { it.measure }, { it.code })` compara en cascada.
- `_observations.removeAll { it.code == code }` borra por condición; compara `size` antes y después para el conteo.
- Para el informe, `listOf(header(domain)) + filas + "Total: $size"` y `joinToString("\n")`.
- Si `parse` lanza en vez de devolver `null`, falta el `try`/`catch` alrededor del constructor.

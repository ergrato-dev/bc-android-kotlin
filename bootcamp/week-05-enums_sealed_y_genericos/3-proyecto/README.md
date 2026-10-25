# Proyecto — Modelo tipado de la Bitácora

⏱️ 4 h · Capa 4 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora sin `String`s sueltos. `Level` es un `enum`, `Code` es una `value class`, y leer líneas devuelve un `ParseResult` que dice **por qué** se descarta cada una. Al terminar, el usuario puede corregir sus datos en lugar de adivinar.

## Dónde trabajas

En **tu repositorio**, rama `week-05`. El `starter/` trae `Observation` (ya con `Code`), `Logbook` con `add`, `remove` y `observations` hechos, y `Base.kt`; **usa tus versiones y tus nombres del dominio**. Este paso es un refactor: apóyate en los errores del compilador.

## Pasos

1. **`Code`**: `value class` con `require` (`"code en blanco"`) y orden natural.
2. **`Level`**: `enum` con `Level.of(measure, mediumFrom, highFrom)`. Una medida negativa o `NaN` lanza `IllegalArgumentException` con un mensaje que incluye el valor. Borra la antigua función `level(...)` que devolvía `String`.
3. **`Observation.level`**: ahora es un `Level`.
4. **`parseLine`**: devuelve `ParseResult.Valid` o `ParseResult.Invalid(line, reason)`. Los motivos tienen un texto **exacto** (están en el comentario de la función). Recorta los espacios de cada campo y no dejes escapar ninguna excepción.
5. **`countByLevel` y `report`**: el mapa usa `Level` como clave y siempre tiene los tres niveles (`Level.entries.associateWith`). El informe usa `level.name`.
6. **`fromLines`**: devuelve un `ImportResult(logbook, invalid)`. Lo válido se añade; lo inválido se guarda **en orden** con su motivo.
7. **`topBy`**: función genérica con `T : Comparable<T>`. Ordena de mayor a menor por `key(it)` y desempata por `code` ascendente. Pruébala con `{ it.measure }` y con `{ it.quantity }`.
8. **Escribe al menos 9 tests** en `LogbookTest.kt`, calculados **a mano**:
   - `Level.of` en los límites (`9.99`, `10.0`, `49.99`, `50.0`) y con valores inválidos,
   - el orden natural de `Code`,
   - `parseLine` con una línea válida y **un test por cada motivo** de rechazo,
   - `countByLevel` con una bitácora vacía y con datos,
   - el texto exacto de `report`,
   - `fromLines` con varias líneas inválidas (comprueba el **orden** de `invalid`),
   - `topBy` con las dos claves y con `n` negativo.
9. **Cambia `Main.kt`** por datos de tu dominio (con descartes de motivos distintos) y ejecuta `./gradlew run`.
10. **Abre el pull request** `week-05` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, ningún `String` como nivel o código en la API pública, sin `else` en `when` sobre `enum`/`sealed`, tests con valores calculados a mano.

## Pistas

- Cambia primero el tipo y deja que el compilador te enseñe cada sitio que hay que arreglar.
- `line.split(";").map { it.trim() }` recorta todos los campos de una vez.
- Un `try`/`catch (e: IllegalArgumentException)` alrededor de `Observation(Code(...), ...)` recoge los errores de `Code` y de `Observation`; `e.message` es el motivo.
- `compareByDescending<Observation> { key(it) }.thenBy { it.code }` combina la clave genérica con el desempate.
- Si `topBy` no compila, falta la cota `T : Comparable<T>` en la firma.

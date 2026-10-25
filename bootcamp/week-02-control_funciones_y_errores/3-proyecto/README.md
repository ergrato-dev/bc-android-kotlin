# Proyecto — Reglas y validación de la Bitácora

⏱️ 4 h · Capa 1 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Sobre el `core` de la semana 1, añades las **reglas del dominio**: cada observación recibe un nivel (`LOW`, `MEDIUM`, `HIGH`), los datos se validan al entrar, y el programa lee líneas de texto que pueden venir mal formadas sin romperse.

## Dónde trabajas

En **tu repositorio** `bc-android-kotlin-<tu-dominio>`, rama `week-02`. El `starter/` contiene la base de la semana 1 (`Observation.kt`) para que compile sola; **si ya tienes tu versión, úsala y conserva tus nombres del dominio**. Copia `Rules.kt`, `RulesTest.kt` y `Main.kt` y adáptalos.

## Pasos

1. **`level`**: `when` con condiciones y umbrales por defecto (10.0 y 50.0). Una medida negativa o `NaN` lanza `IllegalArgumentException` (`require`) con un mensaje que incluya el valor. **Ajusta los umbrales** a algo con sentido en tu dominio y anótalo en el README del repositorio.
2. **`validated`**: devuelve la observación o lanza `IllegalArgumentException` diciendo qué regla falla (código en blanco, descripción en blanco, cantidad negativa, medida negativa o `NaN`).
3. **`parseObservation`**: una línea `CODE;descripción;cantidad;medida` → `Observation?`. Devuelve `null` si no tiene 4 partes, si cantidad o medida no son números, o si `validated` la rechaza. **No dejes escapar ninguna excepción.**
4. **`parseValid` y `countInvalid`**: un `for` y una lista mutable; no inventes otra forma de decidir qué es válido.
5. **`summary`**: cabecera, una fila por observación válida con su nivel al final, y `Descartadas: N`. Sin líneas válidas, escribe también `(sin observaciones)`.
6. **Escribe al menos 6 tests** en `RulesTest.kt`, calculados **a mano** antes de ejecutar. Obligatorios:
   - los límites de `level` (justo en cada umbral y justo debajo),
   - un `assertFailsWith` para `level` y otro para `validated`,
   - `parseObservation` válida y los cuatro casos inválidos (partes, cantidad, medida, validación),
   - `parseValid` y `countInvalid` con una lista mezclada,
   - el texto exacto de `summary`.
7. **Cambia `Main.kt`** por líneas de tu dominio (una inválida a propósito) y ejecuta `./gradlew run`.
8. **Abre el pull request** `week-02` → `main` con la salida de `./gradlew test` y la de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, sin `!!`, sin `catch` vacíos, tests propios con valores calculados a mano.

## Pistas

- `linea.split(";")` devuelve una lista; comprueba su `size` antes de indexar.
- `toIntOrNull()` y `toDoubleOrNull()` ya devuelven `null` si no se puede convertir: no necesitas `try` para ellos.
- Para `validated` sí necesitas `try`/`catch` en `parseObservation`: captura `IllegalArgumentException`, no `Exception`.
- Los mensajes de error deben decir qué llegó: `"measure inválida: -3.0"`.

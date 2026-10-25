# Proyecto — Bitácora serializable y DSL

⏱️ 4 h · Capa 7 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

La Bitácora se guarda como un **archivo JSON versionado** y se construye con un **DSL**. `importFile` es tolerante: de un archivo con observaciones rotas importa las buenas y cuenta las malas con su posición y su motivo; un archivo de una versión futura se rechaza con claridad, y nada lanza una excepción hacia fuera.

## Dónde trabajas

En **tu repositorio**, rama `week-08`. El `starter/` trae `Code`, `Level` y `Observation` de las semanas anteriores, sin anotar; **usa tus versiones y tus nombres del dominio**. Lo nuevo está en `LogbookFile.kt`, `Import.kt` y `Dsl.kt`, con los formatos y los motivos exactos en los comentarios. El plugin y la dependencia ya están en el `build.gradle.kts`.

## Pasos

1. **Modelo serializable**: `@Serializable` en `Code` (se escribe como texto suelto), `Level` (`@SerialName` en minúsculas) y `Observation`, y en `LogbookFile`.
2. **`logbookJson`**: una sola instancia; ignora claves desconocidas y escribe los valores por defecto (así `version` y `counts` siempre salen).
3. **`encodeFile`**: texto compacto o con sangría (`pretty`).
4. **`importFile`**: tolerante y sin excepciones. Lee con el árbol: JSON y raíz, `version` (ausente vale 1; una futura se rechaza **antes** de decodificar nada más), `domain`, `observations`. Decodifica cada observación por separado y clasifica el fallo (campo ausente, tu `init`, valor no válido).
5. **`logbookFile`**: el DSL con `@DslMarker`, valores por defecto, `description` obligatoria, códigos únicos, `domain` no vacío y `counts` siempre con los tres niveles.
6. **Cambia `Main.kt`** por datos de tu dominio: construye un archivo con el DSL, escríbelo con sangría, vuelve a importarlo, e importa un texto con observaciones rotas de **motivos distintos** y una versión futura. Ejecuta `./gradlew run`.
7. **Escribe al menos 8 tests** en `FileTest.kt`, con el valor esperado escrito **a mano**:
   - el JSON de un `Code` (texto suelto) y de un `Level` (minúsculas),
   - el texto exacto de un archivo completo, con y sin `counts`,
   - la ida y vuelta (DSL → `encodeFile` → `importFile`),
   - `version` ausente y claves desconocidas,
   - una versión futura,
   - JSON roto, raíz que no es objeto, `domain` ausente y `observations` que no es un array,
   - observaciones rotas, con **posición y motivo** (campo ausente, `init`, tipo equivocado, elemento que no es un objeto) y el orden de lo válido,
   - el DSL: código repetido, descripción ausente, `domain` en blanco y `counts`.
8. **Abre el pull request** `week-08` → `main` con la salida de `./gradlew test` y de `./gradlew run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, una sola instancia de `Json`, ningún `catch` vacío ni `catch (e: Exception)`, la importación no lanza, `@DslMarker` en los constructores, tests con valores calculados a mano.

## Pistas

- `root["version"]` devuelve `null` si no existe: úsalo para el valor por defecto.
- `(x as? JsonPrimitive)?.intOrNull` no lanza; `x.jsonPrimitive` sí (`IllegalArgumentException`).
- `decodeFromJsonElement<Observation>(element)` lanza `SerializationException`; la causa es tu `IllegalArgumentException` cuando falla un `require`.
- Captura `MissingFieldException` **antes** que `SerializationException`, y recuerda el `@OptIn`.
- Dentro del miembro `observation` del constructor, construye con `ObservationBuilder(code).apply(block).build()`.
- Para el texto esperado de los tests, cadenas de comillas triples: `"""{"version":1,...}"""`.
- Un `Double` como `3.0` se escribe `3.0`, no `3`.

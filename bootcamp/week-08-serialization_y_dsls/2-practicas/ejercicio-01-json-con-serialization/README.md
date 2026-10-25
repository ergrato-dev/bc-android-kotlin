# Ejercicio 01 — JSON con serialization

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Escribir y leer JSON con un formato controlado: valores por defecto, nombres propios, enums, `value class`, jerarquías selladas, errores explicados y un serializador propio.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`. Los tests ya están escritos en `src/test/kotlin/MainTest.kt`: léelos, dicen el formato exacto.

1. **`Reading`**: ponle `@Serializable` y escribe con la instancia `json`. Comprueba que `unit` desaparece cuando vale `"C"`. Cambia `encodeDefaults` a `true` y observa; deja el valor por defecto de la biblioteca.
2. **`decodeReading`**: configura `json` con `ignoreUnknownKeys = true`. Quítalo y mira qué excepción sale.
3. **`Sensor`**: `@SerialName("sensor_id")` y `@Transient`. Prueba a quitar el valor por defecto de `cache` y mira el error del compilador.
4. **`Priority` y `Tag`**: nombres en minúsculas con `@SerialName`, y una `value class` que se escribe como texto suelto.
5. **`Event`**: una jerarquía sellada con discriminador. Escribe con `encodeToString<Event>(e)` y comprueba qué pasa si no pasas el tipo base.
6. **`decodeSafely`**: devuelve `Ok` o `Invalid(motivo)` con los motivos exactos del comentario. Captura primero la excepción más específica.
7. **`Fixed2Serializer`**: el `Double` viaja como texto con dos decimales. Úsalo en `Measurement.value` con `@Serializable(with = ...)`.
8. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Serializer for class 'X' is not found` | Falta `@Serializable` (también en los subtipos de `Event` y en `Tag`) |
| `Redundant creation of Json format` | Crea `Json { }` una sola vez, fuera de las funciones |
| `Encountered an unknown key 'extra'` | Falta `ignoreUnknownKeys = true` en `json` |
| El JSON de `Event` no lleva `type` | Escribe con el tipo base: `json.encodeToString<Event>(e)` |
| `Level does not contain element with name 'high'` | Los nombres del JSON salen del `@SerialName`; revisa que esté en cada constante |
| `This declaration needs opt-in` en `decodeSafely` | `@OptIn(ExperimentalSerializationApi::class)` en la función (por `MissingFieldException`) |
| `decodeSafely` devuelve el motivo de la biblioteca | La causa de la excepción es tu `IllegalArgumentException`: usa `e.cause` |
| `Measurement` escribe `{"value":12.5}` | Falta `@Serializable(with = Fixed2Serializer::class)` en la propiedad |

## Entrega

Los 7 tests en verde. No hace falta `NOTAS.md`.

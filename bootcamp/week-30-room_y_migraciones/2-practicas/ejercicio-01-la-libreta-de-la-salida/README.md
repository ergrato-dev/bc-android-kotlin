# Ejercicio 01 — La libreta de la salida

⏱️ 2 h · Teoría previa: 01, 02 y 05

## Objetivo

Cada salida al campo tiene su lista de cosas que llevar. Al terminar, las dos tablas (salidas y cosas) se cuidan solas: no hay una cosa de una salida que no existe, al borrar la salida se van sus cosas, y no se repite un nombre dentro de la misma salida. La lista de lo que falta por guardar es un `Flow` que se actualiza con cada cambio, y copiar una salida es todo o nada. Todo en la JVM, con Room 3 y el SQLite empaquetado.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con Room 3.0.3 (KSP 2.3.13 y el plugin `androidx.room3`), `sqlite-bundled` 2.7.1 y `kotlinx-coroutines` 1.11.0. En `Libreta.kt` están las dos entidades, `TripWithItems` (ya hecha) y el DAO, con comentarios `Paso N` donde falta algo. Compila: Room genera el código aunque las consultas no hagan aún lo que piden los tests.

Los tests (`src/test/kotlin/dev/bitacora/LibretaTest.kt`, no los toques) abren una base de datos en memoria por test. Al empezar, fallan 7 de 8.

## Pasos

1. **Lee y ejecuta** `./gradlew test`. Abre `build/generated/ksp/main/kotlin/dev/bitacora/LibretaDao_Impl.kt` y busca el SQL de `pending`: es el de tu `@Query`, tal cual.
2. **Las reglas de la tabla** (`Item`): una `ForeignKey` a `Trip` (`parentColumns = ["id"]`, `childColumns = ["tripId"]`, `onDelete = ForeignKey.CASCADE`) y un `Index("tripId", "name", unique = true)` (teoría 02, apartado 2).
3. **Lo que falta por guardar**: `pending` solo con las que no tienen `packed`, por nombre.
4. **Las salidas con sus cosas**: `trips` por nombre, con `@Transaction` (apartado 6).
5. **Copiar una salida**: con `@Transaction`, inserta la salida nueva y, por cada cosa de la vieja (`itemsOf`), una con el mismo nombre y sin guardar. Devuelve el id nuevo (apartado 5).
6. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`.
7. **`NOTAS.md`** (en `starter/`):
   - (a) ¿Qué excepción y qué mensaje da `anItemNeedsItsTrip` antes del paso 2? ¿Y después? ¿Quién la lanza, Room o SQLite?
   - (b) Cambia el índice a `Index("name", "tripId", unique = true)` y compila. ¿Qué dice KSP, y por qué importa al borrar una salida? Restáuralo.
   - (c) Quita `@Transaction` de `trips` y compila. ¿Qué dice KSP? ¿Por qué no rompe el build, si está `allWarningsAsErrors`? Restáuralo.
   - (d) En `pendingFollowsTheChanges`, ¿por qué se recoge en `Dispatchers.Default` y se espera con un `Channel`, y no con `delay`?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `No parameter with name 'parentColumn' found` | Room 3 pide listas: `parentColumns = ["id"]`, como en `TripWithItems` |
| En `pendingFollowsTheChanges`, la tercera emisión sigue con `linterna` | `pending` no filtra `packed`: `AND NOT packed` |
| `tripsComeWithTheirItems` da `Páramo` antes que `Costa` | Falta `ORDER BY name` |
| `copyingATripCopiesItsItemsUnpacked` da `[]` | `copy` no inserta las cosas, o las copia con `packed = true` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 7.

# Ejercicio 01 — Clases y propiedades

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Escribir cinco clases pequeñas donde el objeto **no pueda quedar en un estado imposible**.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`. Cada miembro tiene su `TODO("Paso N")`.

1. **`Counter`**: `isZero` es una propiedad calculada; `increment(by)` exige `by > 0` (`require`) y suma; `reset()` vuelve a 0. Fíjate en que `value` ya declara `private set`: intenta asignarlo desde `main` y mira el error.
2. **`Temperature`**: valida `initialCelsius` en `init` **y** el valor en el `set` de `celsius` (usa `field`). `fahrenheit` es calculada. Si el `set` rechaza un valor, `celsius` debe conservar el anterior.
3. **`Rectangle`**: `init` exige lados `> 0`. Lee el constructor secundario ya escrito y explica en un comentario por qué delega con `this(...)`. `area`, `perimeter` e `isSquare` son calculadas.
4. **`Point`**: `operator fun plus` y `distanceTo` (añade `import kotlin.math.sqrt`). Comprueba que `copy` funciona sin haberlo escrito.
5. **`Playlist`**: `_songs` es privada; `songs` devuelve **una copia**; `add` rechaza un título repetido y `seconds <= 0` (`require`). `totalSeconds` es calculada.
6. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `StackOverflowError` en `Temperature` | En el `set` escribe `field = value`, no `celsius = value` |
| `Temperature(-300.0)` no lanza | Falta la validación en `init` (el `set` no se llama al inicializar) |
| `Rectangle(0.0, 2.0)` no lanza | `init` va después del constructor secundario; comprueba que no lo borraste |
| `playlistSongsIsASnapshot` falla | `songs` debe devolver `_songs.toList()`, no `_songs` |
| `Unresolved reference: sqrt` | Falta `import kotlin.math.sqrt` |

## Entrega

Los 6 tests en verde. No hace falta `NOTAS.md`.

# Ejercicio 02 — Funciones y errores

⏱️ 2 h · Teoría previa: 03, 04 y 05

## Objetivo

Decidir en cada función si devolver un valor, `null`, lanzar o devolver un `Result`, y probarlo.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`dividir`**: `require(b != 0.0) { "..." }` con un mensaje que no esté vacío.
2. **`parseEntero`**: `trim()` y `toIntOrNull()`. Devuelve `null` si no es un entero (`"4.2"` y `""` tampoco lo son).
3. **`parseEnteroOLanza`**: usa `parseEntero` con `?:` y `throw IllegalArgumentException("No es un entero: '$texto'")`. El mensaje debe ser exacto.
4. **`promedio`**: `vararg`, `require(valores.isNotEmpty())` y la suma entre la cantidad.
5. **`formatear`**: `String.format(Locale.ROOT, ...)` con `decimales` y `ancho`. Con `ancho = 0` no escribas ancho en el formato.
6. **`leerSeguro`**: `try`/`catch` **como expresión** capturando `IllegalArgumentException`. Un parámetro por defecto (`porDefecto = -1`).
7. **`resultado`**: `runCatching { ... }.fold(onSuccess = ..., onFailure = ...)`.
8. **Responde en `NOTAS.md`**:
   - ¿Por qué `parseEntero` devuelve `null` y `dividir` lanza una excepción?
   - ¿Qué pasaría si `leerSeguro` capturara `Exception` en vez de `IllegalArgumentException`? Da un caso concreto.
   - ¿Cuándo usarías `runCatching` y cuándo no?
9. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `MissingFormatWidthException` | El formato quedó como `%0.2f`; con ancho 0 usa `%.2f` |
| `formatear(2.5, decimales = 0)` da `2` | `String.format` redondea hacia arriba (HALF_UP): `3`. Si te da otra cosa, revisa el formato |
| El test del mensaje falla por una comilla | El mensaje exacto es `No es un entero: 'abc'` |
| `Unresolved reference: Locale` | Falta `import java.util.Locale` |
| Warning que rompe el build | Variable o parámetro sin usar en un `catch`: usa `catch (_: IllegalArgumentException)` |

## Entrega

Los 6 tests en verde y `NOTAS.md` con el paso 8.

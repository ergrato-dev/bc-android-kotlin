# Ejercicio 02 — Mide los tipos

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Comprobar con código los tamaños, los límites y las trampas de los tipos numéricos.

## Pasos

Trabaja dentro de `starter/`. Cada función tiene su `TODO("Paso N")`.

1. **Tamaños.** `tamanosEnBytes()`: `Byte`, `Short`, `Int` y `Long`, usando `SIZE_BYTES`. Debe dar `[1, 2, 4, 8]`.
2. **Divisiones.** `divisionEntera` y `divisionReal`. Antes de ejecutar, escribe en `NOTAS.md` qué darán `7/2`, `-7/2` y `7/2.0`.
3. **Desbordamiento.** `sumaConEnvoltura` (suma normal) y `sumaExacta` (`Math.addExact`). Comprueba qué da `Int.MAX_VALUE + 1` en cada una.
4. **Truncado.** `truncar(d)` con `toInt()`. ¿Qué pasa con `-3.7`?
5. **Decimales.** `casiIguales` con `kotlin.math.abs` y una tolerancia. Mira en el test por qué `0.1 + 0.2 == 0.3` es falso.
6. **Provoca un error de tipos.** En un archivo nuevo, escribe `val x: Int = 3.7` y ejecuta `./gradlew compileKotlin`. Copia el mensaje. Corrígelo de dos formas (`toInt()` y cambiando el tipo a `Double`) y borra el archivo. Explica en `NOTAS.md` por qué Kotlin no convierte solo.
7. **Ejecuta** `./gradlew run` y compara con lo que escribiste en el paso 2.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `ArithmeticException: / by zero` | Dividiste entre `0` con enteros; con `Double` daría `Infinity` |
| Un test de `Double` falla por poco | No uses `==`, usa `casiIguales` |
| `Unresolved reference: abs` | Falta `import kotlin.math.abs` o usa `kotlin.math.abs(...)` |

## Entrega

Los 5 tests en verde y `NOTAS.md` con las respuestas de los pasos 2, 3 y 6.

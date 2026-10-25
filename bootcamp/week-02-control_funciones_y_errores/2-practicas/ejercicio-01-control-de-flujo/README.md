# Ejercicio 01 — Control de flujo

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Poner en verde 6 tests usando `when`, rangos, `for`, `while` y `return` dentro de un bucle.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`. Cada función tiene su `TODO("Paso N")`.

1. **`nombreDelDia`**: `when` con argumento como expresión. Fíjate en que `else` es obligatorio.
2. **`fizzBuzz`**: `when` **sin** argumento. Piensa antes en el orden de las ramas y comprueba con el 30.
3. **`sumaPares`**: un `for` con `2..hasta step 2`. ¿Qué pasa con `hasta = 1`? ¿Y con `-5`? Responde antes de ejecutar.
4. **`cuentaAtras`**: `downTo` y una `mutableListOf<Int>()` donde acumular. `cuentaAtras(0)` debe dar una lista vacía.
5. **`esPrimo`**: casos pequeños primero (`n < 2`), luego un `while (d * d <= n)` que haga `return false` en cuanto encuentre un divisor.
6. **`primerMultiplo`**: recorre `(mayorQue + 1)..hasta` y devuelve el primero múltiplo de `de`; si el bucle termina sin encontrarlo, `null`. Fíjate en que `hasta` tiene valor por defecto.
7. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `'when' expression must be exhaustive` | Falta el `else` |
| `fizzBuzz(30)` da `"Fizz"` | Tienes la rama de 3 antes que la de 15 |
| `sumaPares(10)` no da 30 | Revisa el rango: `2..hasta step 2` incluye el 10 |
| `esPrimo(2)` falla | Con `d * d <= n` y `d = 2`, el bucle no entra: debe devolver `true` |
| `NotImplementedError` | Te falta una función por implementar |

## Entrega

Los 6 tests en verde. No hace falta `NOTAS.md`.

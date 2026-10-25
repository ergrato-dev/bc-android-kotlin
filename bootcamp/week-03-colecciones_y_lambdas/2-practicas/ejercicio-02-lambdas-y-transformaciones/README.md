# Ejercicio 02 — Lambdas y transformaciones

⏱️ 2 h · Teoría previa: 02, 03, 04 y 05

## Objetivo

Escribir tus propias funciones de orden superior y encadenar transformaciones.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`aplicarDosVeces`**: recibe una función `(Int) -> Int`. Una línea.
2. **`crearSumador`**: devuelve una lambda que captura `n`. Prueba en `main` que `crearSumador(3)` y `crearSumador(7)` no se mezclan.
3. **`sumaDeCuadradosPares`**: `filter`, `map` y `sum` encadenados. ¿Qué devuelve con la lista vacía?
4. **`contarSi`**: implementa tú el `count { }`, con un `for` y un contador. Recibe `predicado: (Int) -> Boolean`.
5. **`maxPor`**: implementa tú el `maxByOrNull`. Cuidado con la lista vacía y con el empate (gana el primero).
6. **`mediaPorAlumno`**: `groupBy({ it.first }, { it.second })` y `mapValues`.
7. **`top3`**: `sortedDescending().take(3)`.
8. **`separarParesImpares`**: `partition`. Observa el tipo `Pair<List<Int>, List<Int>>`.
9. **Responde en `NOTAS.md`**:
   - Reescribe `sumaDeCuadradosPares` con `asSequence()`. ¿Cambia el resultado? ¿Cuándo valdría la pena?
   - ¿Qué hace `crearSumador(10)(5)`? Explica por qué la lambda "recuerda" `n`.
   - Elige un `for` de tu proyecto de la semana 2 y reescríbelo con una función de colección. ¿Queda más claro?
10. **Ejecuta** `./gradlew test` (6 de 6) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Type mismatch` al pasar una lambda | Revisa el tipo del parámetro: `(Int) -> Boolean` devuelve `Boolean` |
| `crearSumador(3)(4)` no compila | Escribe `crearSumador(3)(4)`: el segundo paréntesis llama a la función devuelta |
| `maxPor` devuelve el último en un empate | Compara con `>` estricto, no con `>=` |
| `mediaPorAlumno` da `NaN` | Dividiste entre 0: la lista de notas de un alumno nunca está vacía si vino de `groupBy` |

## Entrega

Los 6 tests en verde y `NOTAS.md` con el paso 9.

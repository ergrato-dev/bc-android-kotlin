# Ejercicio 02 — Value classes y genéricos

⏱️ 2 h · Teoría previa: 03, 04 y 05

## Objetivo

Crear tipos que no se confunden y escribir código genérico con restricciones, varianza y `reified`.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`Meters` y `Email`**: validan en `init` con `require` y un mensaje. `Meters` se suma con `+`. Comprueba que `Meters(3.0)` y un `Double` no se pueden mezclar (inténtalo y copia el error).
2. **`Stack<T>`**: pila genérica con lista privada. `popOrNull` con `removeLastOrNull`.
3. **`firstOrDefault`, `maxOfTwo`, `clamp`**: genéricas. Fíjate en la cota `T : Comparable<T>`; prueba a quitarla y lee el error. `clamp` exige `min <= max`.
4. **`Box<out T>`**: ya está escrito. En el test `covariantBoxesFitWhereAnyIsExpected`, quita `out` y comprueba qué deja de compilar.
5. **`Outcome<T>`**: implementa `map` con un `when` exhaustivo (un `Err` pasa tal cual) y `parseAge` (`"no es un entero: '<texto>'"` y `"edad fuera de rango: N"`).
6. **`countOfType`**: `inline` + `reified`. Prueba a quitar `reified` y lee el error.
7. **Responde en `NOTAS.md`**:
   - ¿Por qué `Err` es `Outcome<Nothing>` y no `Outcome<T>`? ¿Qué se rompería sin `out T`?
   - Una `value class Email` frente a un `typealias Email = String`: ¿qué error evita la primera?
   - ¿Qué te dijo el compilador en los pasos 3, 4 y 6? Copia los tres mensajes.
8. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Value class cannot have backing fields` | Una `value class` solo tiene la propiedad del constructor |
| `Unresolved reference: compareTo` | Falta la cota `T : Comparable<T>` |
| `Cannot use 'T' as reified type parameter` | La función debe ser `inline` |
| `Type mismatch` en `map` | `Err` ya es un `Outcome<Nothing>`; devuélvelo con `this` |
| `Type parameter T is declared as 'out' but occurs in 'in' position` | Quita `out` o no uses `T` como parámetro |

## Entrega

Los 7 tests en verde y `NOTAS.md` con el paso 7.

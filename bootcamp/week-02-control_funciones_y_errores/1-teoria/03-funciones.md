# Funciones

> Una función es un trozo de código con nombre, entradas tipadas y una salida tipada. En Kotlin las funciones son tan baratas que se escriben muchas y pequeñas: es la forma de que el código se lea como una explicación.

## 🎯 Objetivos

- Declarar funciones con parámetros, tipo de retorno y cuerpo de expresión
- Usar **valores por defecto** y **argumentos con nombre**
- Recibir un número variable de argumentos con `vararg`
- Declarar funciones locales
- Reconocer cuándo una función debe devolver `null`

## 1. Anatomía

![Anatomía de una función](../0-assets/03-anatomia-funcion.svg)

```kotlin
fun formatear(valor: Double, decimales: Int = 2): String {
    return String.format(Locale.ROOT, "%.${decimales}f", valor)
}

fun formatear(valor: Double, decimales: Int = 2) =        // equivalente
    String.format(Locale.ROOT, "%.${decimales}f", valor)
```

- Los **parámetros** siempre llevan tipo. El tipo de retorno va después de `:`.
- Con cuerpo de bloque (`{ }`) hay que escribir `return` y el tipo de retorno (salvo `Unit`).
- Con **cuerpo de expresión** (`=`), el tipo de retorno se infiere. En funciones públicas conviene escribirlo igualmente: es documentación y evita cambios accidentales.
- Los parámetros son **inmutables** (`val`): no se les puede reasignar.

## 2. Valores por defecto y argumentos con nombre

```kotlin
fun level(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): String = ...

level(12.0)                        // usa 10.0 y 50.0
level(12.0, 5.0)                   // por posición
level(12.0, highFrom = 20.0)       // con nombre: te saltas mediumFrom
level(measure = 12.0, highFrom = 20.0)
```

Kotlin no necesita sobrecargar la misma función cinco veces con distintos números de parámetros: un valor por defecto lo resuelve. **Con nombre se lee mejor** cuando hay varios `Double` o `Int` seguidos; el compilador no puede avisarte de que cambiaste el orden de dos números.

## 3. `vararg`

```kotlin
fun promedio(vararg valores: Double): Double = valores.sum() / valores.size

promedio(1.0, 2.0, 3.0)
val notas = doubleArrayOf(4.0, 5.0)
promedio(*notas)                 // el * "esparce" un array como argumentos
```

Dentro de la función, `valores` es un array. Solo un parámetro puede ser `vararg`. ¡Cuidado! `promedio()` sin argumentos es válido, así que decide qué hacer con el caso vacío (en el ejercicio, `require`).

## 4. Funciones locales

Una función puede declararse dentro de otra, si solo la usa ella:

```kotlin
fun esPrimo(n: Int): Boolean {
    fun divisiblePor(d: Int) = n % d == 0
    ...
}
```

Pueden usar las variables y parámetros de la función que las rodea, y no ensucian el nivel superior del archivo.

## 5. ¿Valor, `null` o excepción?

| Situación | Qué devolver |
| --- | --- |
| El resultado siempre existe | El valor |
| Puede no existir y es **normal** (buscar, parsear texto del usuario) | `T?` (`null`) |
| Los argumentos rompen el contrato de la función (un bug del que llama) | Lanzar `IllegalArgumentException` |
| El estado interno impide continuar | Lanzar `IllegalStateException` |

Un nombre ayuda: `parseEntero` devuelve `Int?`; `parseEnteroOLanza` lanza. Que el nombre diga la política evita sorpresas.

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| `A 'return' expression required in a function with a block body` | Cuerpo de bloque sin `return` | Añade `return` o usa `=` |
| `Cannot find a parameter with this name: x` | Nombre de argumento mal escrito | Revisa la firma |
| `Val cannot be reassigned` sobre un parámetro | Los parámetros son `val` | Copia a una variable local |
| `promedio()` da `NaN` | `0.0 / 0` con un `vararg` vacío | Valida con `require` |

## ✅ Qué debes saber al terminar

- Firma, cuerpo de bloque y cuerpo de expresión.
- Valores por defecto y argumentos con nombre.
- `vararg` y el `*` para esparcir.
- Cuándo devolver `null` y cuándo lanzar.

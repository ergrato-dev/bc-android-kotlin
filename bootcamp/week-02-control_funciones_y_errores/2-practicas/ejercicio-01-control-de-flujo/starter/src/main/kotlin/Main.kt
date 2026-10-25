// Paso 1: 1 = "Lunes" ... 7 = "Domingo"; cualquier otro número, "Desconocido". Usa when como expresión.
fun nombreDelDia(n: Int): String = TODO("Paso 1")

// Paso 2: "FizzBuzz" si n es múltiplo de 15, "Fizz" si lo es de 3, "Buzz" si lo es de 5; si no, el número como texto.
fun fizzBuzz(n: Int): String = TODO("Paso 2")

// Paso 3: suma de los pares entre 2 y hasta (ambos incluidos). Usa un for con step. Si hasta < 2, 0.
fun sumaPares(hasta: Int): Int = TODO("Paso 3")

// Paso 4: de desde a 1, de mayor a menor. Usa downTo y acumula en una lista mutable.
fun cuentaAtras(desde: Int): List<Int> = TODO("Paso 4")

// Paso 5: true si n es primo. Menores que 2 no lo son. Para en cuanto encuentres un divisor.
fun esPrimo(n: Int): Boolean = TODO("Paso 5")

// Paso 6: primer número mayor que mayorQue y menor o igual que hasta que sea múltiplo de de; null si no hay.
fun primerMultiplo(de: Int, mayorQue: Int, hasta: Int = 1000): Int? = TODO("Paso 6")

fun main() {
    println((1..15).joinToString(" ") { fizzBuzz(it) })
    println("Cuenta atrás: ${cuentaAtras(5)}")
    println("¿7 es primo? ${esPrimo(7)}")
}

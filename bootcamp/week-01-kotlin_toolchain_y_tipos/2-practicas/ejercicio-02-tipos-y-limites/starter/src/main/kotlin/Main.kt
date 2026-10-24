// Paso 1: tamaño en bytes de Byte, Short, Int y Long, en ese orden. Usa las constantes SIZE_BYTES.
fun tamanosEnBytes(): List<Int> = TODO("Paso 1")

// Paso 2: división de enteros (descarta la parte decimal) y división real.
fun divisionEntera(a: Int, b: Int): Int = TODO("Paso 2")
fun divisionReal(a: Int, b: Int): Double = TODO("Paso 2")

// Paso 3: Int desborda en silencio; Math.addExact lanza ArithmeticException.
fun sumaConEnvoltura(a: Int, b: Int): Int = TODO("Paso 3")
fun sumaExacta(a: Int, b: Int): Int = TODO("Paso 3")

// Paso 4: de Double a Int, quitando los decimales (3.7 -> 3, -3.7 -> -3).
fun truncar(d: Double): Int = TODO("Paso 4")

// Paso 5: los Double no se comparan con ==; se comparan con una tolerancia.
fun casiIguales(a: Double, b: Double, epsilon: Double = 1e-9): Boolean = TODO("Paso 5")

fun main() {
    println("Tamaños: ${tamanosEnBytes()}")
    println("7 / 2 = ${divisionEntera(7, 2)} y ${divisionReal(7, 2)}")
    println("MAX + 1 = ${sumaConEnvoltura(Int.MAX_VALUE, 1)}")
}

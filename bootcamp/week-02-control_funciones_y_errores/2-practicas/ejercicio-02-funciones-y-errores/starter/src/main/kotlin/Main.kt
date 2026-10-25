// Paso 1: a / b. Si b es 0.0, lanza IllegalArgumentException con require y un mensaje.
fun dividir(a: Double, b: Double): Double = TODO("Paso 1")

// Paso 2: convierte a Int, ignorando espacios al principio y al final. Si no es un entero, null.
fun parseEntero(texto: String): Int? = TODO("Paso 2")

// Paso 3: como parseEntero, pero lanza IllegalArgumentException("No es un entero: '<texto>'") en vez de devolver null.
fun parseEnteroOLanza(texto: String): Int = TODO("Paso 3")

// Paso 4: media de los valores. Sin valores, lanza IllegalArgumentException (require).
fun promedio(vararg valores: Double): Double = TODO("Paso 4")

// Paso 5: formatea con String.format(Locale.ROOT, ...), "decimales" decimales y "ancho" columnas.
// Ojo: con ancho 0 el especificador es "%.2f", no "%0.2f" (el 0 sería otra opción y falla).
fun formatear(valor: Double, decimales: Int = 2, ancho: Int = 0): String = TODO("Paso 5")

// Paso 6: el entero de texto, o porDefecto si no lo es. Usa try/catch como expresión.
fun leerSeguro(texto: String, porDefecto: Int = -1): Int = TODO("Paso 6")

// Paso 7: "OK:<número>" o "ERROR:<mensaje>". Usa runCatching y fold.
fun resultado(texto: String): String = TODO("Paso 7")

fun main() {
    println(formatear(3.14159))
    println(formatear(3.14159, decimales = 4, ancho = 10))
    println(resultado("42"))
    println(resultado("abc"))
}

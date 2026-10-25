// Paso 1: la lista sin repetidos, conservando el orden de la primera aparición.
fun sinDuplicados(lista: List<Int>): List<Int> = TODO("Paso 1")

// Paso 2: cuántas veces aparece cada letra. Ignora mayúsculas y todo lo que no sea letra.
fun frecuencias(texto: String): Map<Char, Int> = TODO("Paso 2")

// Paso 3: la palabra más larga; null si no hay palabras. En empate, la primera.
fun masLarga(palabras: List<String>): String? = TODO("Paso 3")

// Paso 4: los elementos que están en los dos conjuntos / solo en el primero.
fun interseccion(a: Set<Int>, b: Set<Int>): Set<Int> = TODO("Paso 4")
fun soloEnPrimero(a: Set<Int>, b: Set<Int>): Set<Int> = TODO("Paso 4")

// Paso 5: los últimos n elementos; si hay menos de n, todos. n negativo: require.
fun ultimos(lista: List<Int>, n: Int): List<Int> = TODO("Paso 5")

// Paso 6: la cantidad de la clave, o 0 si no existe. Y un mapa NUEVO con la cantidad sumada (el original no cambia).
fun cantidadDe(inventario: Map<String, Int>, clave: String): Int = TODO("Paso 6")
fun agregar(inventario: Map<String, Int>, clave: String, cantidad: Int): Map<String, Int> = TODO("Paso 6")

fun main() {
    println(sinDuplicados(listOf(3, 1, 3, 2, 1)))
    println(frecuencias("Hola Mundo"))
    println(masLarga(listOf("sol", "luna", "estrella")))
}

// Paso 1: aplica f dos veces: f(f(x)).
fun aplicarDosVeces(f: (Int) -> Int, x: Int): Int = TODO("Paso 1")

// Paso 2: devuelve una función que suma n a su argumento (captura n).
fun crearSumador(n: Int): (Int) -> Int = TODO("Paso 2")

// Paso 3: la suma de los cuadrados de los números pares. Encadena filter, map y sum.
fun sumaDeCuadradosPares(lista: List<Int>): Int = TODO("Paso 3")

// Paso 4: tu propio contarSi: cuántos elementos cumplen el predicado. Con un for, sin usar count.
fun contarSi(lista: List<Int>, predicado: (Int) -> Boolean): Int = TODO("Paso 4")

// Paso 5: el elemento con mayor clave(elemento); null si la lista está vacía. Con un for, sin usar maxBy.
fun maxPor(lista: List<String>, clave: (String) -> Int): String? = TODO("Paso 5")

// Paso 6: la nota media de cada alumno. Entrada: pares (alumno, nota). Usa groupBy y mapValues.
fun mediaPorAlumno(notas: List<Pair<String, Double>>): Map<String, Double> = TODO("Paso 6")

// Paso 7: los tres mayores, de mayor a menor (menos de tres si no hay más).
fun top3(lista: List<Int>): List<Int> = TODO("Paso 7")

// Paso 8: separa en (pares, impares) con partition.
fun separarParesImpares(lista: List<Int>): Pair<List<Int>, List<Int>> = TODO("Paso 8")

fun main() {
    println(aplicarDosVeces({ it * 3 }, 2))
    println(crearSumador(10)(5))
    println(sumaDeCuadradosPares(listOf(1, 2, 3, 4)))
}

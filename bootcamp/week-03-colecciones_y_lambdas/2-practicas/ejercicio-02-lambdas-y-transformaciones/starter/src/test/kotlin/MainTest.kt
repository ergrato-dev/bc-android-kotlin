import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MainTest {
    @Test
    fun funcionesComoValores() {
        assertEquals(18, aplicarDosVeces({ it * 3 }, 2))
        assertEquals(15, crearSumador(10)(5))
        val suma3 = crearSumador(3)
        val suma7 = crearSumador(7)
        assertEquals(10, suma3(7) + suma7(0) - 7)
    }

    @Test
    fun cadenaDeTransformaciones() {
        assertEquals(20, sumaDeCuadradosPares(listOf(1, 2, 3, 4)))
        assertEquals(0, sumaDeCuadradosPares(emptyList()))
        assertEquals(0, sumaDeCuadradosPares(listOf(1, 3)))
    }

    @Test
    fun contarSiConUnaLambda() {
        assertEquals(2, contarSi(listOf(1, 5, 8, 20)) { it > 6 })
        assertEquals(0, contarSi(emptyList()) { true })
    }

    @Test
    fun maxPorConUnaClave() {
        assertEquals("carambola", maxPor(listOf("pera", "carambola", "kiwi")) { it.length })
        assertEquals("pera", maxPor(listOf("pera", "kiwi")) { it.length })
        assertNull(maxPor(emptyList()) { it.length })
    }

    @Test
    fun mediaPorAlumnoAgrupa() {
        val notas = listOf("Ana" to 4.0, "Luis" to 3.0, "Ana" to 5.0, "Luis" to 2.0, "Eva" to 4.5)
        assertEquals(mapOf("Ana" to 4.5, "Luis" to 2.5, "Eva" to 4.5), mediaPorAlumno(notas))
    }

    @Test
    fun ordenYSeparacion() {
        assertEquals(listOf(9, 7, 5), top3(listOf(5, 9, 1, 7, 3)))
        assertEquals(listOf(2, 1), top3(listOf(1, 2)))
        assertEquals(Pair(listOf(2, 4), listOf(1, 3, 5)), separarParesImpares(listOf(1, 2, 3, 4, 5)))
    }
}

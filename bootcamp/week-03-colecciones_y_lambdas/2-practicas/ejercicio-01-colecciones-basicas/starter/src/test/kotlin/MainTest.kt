import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MainTest {
    @Test
    fun duplicadosConservanElOrden() {
        assertEquals(listOf(3, 1, 2), sinDuplicados(listOf(3, 1, 3, 2, 1)))
        assertEquals(emptyList(), sinDuplicados(emptyList()))
    }

    @Test
    fun frecuenciasIgnoranMayusculasYEspacios() {
        val esperado = mapOf('h' to 1, 'o' to 2, 'l' to 1, 'a' to 1, 'm' to 1, 'u' to 1, 'n' to 1, 'd' to 1)
        assertEquals(esperado, frecuencias("Hola Mundo"))
        assertEquals(emptyMap(), frecuencias("123 !?"))
    }

    @Test
    fun laPalabraMasLarga() {
        assertEquals("abc", masLarga(listOf("a", "abc", "ab")))
        assertEquals("uno", masLarga(listOf("uno", "dos")))
        assertNull(masLarga(emptyList()))
    }

    @Test
    fun operacionesDeConjuntos() {
        assertEquals(setOf(2, 3), interseccion(setOf(1, 2, 3), setOf(2, 3, 4)))
        assertEquals(setOf(1), soloEnPrimero(setOf(1, 2, 3), setOf(2, 3, 4)))
    }

    @Test
    fun ultimosElementos() {
        assertEquals(listOf(4, 5), ultimos(listOf(1, 2, 3, 4, 5), 2))
        assertEquals(listOf(1, 2), ultimos(listOf(1, 2), 10))
        assertEquals(emptyList(), ultimos(listOf(1, 2), 0))
        assertFailsWith<IllegalArgumentException> { ultimos(listOf(1), -1) }
    }

    @Test
    fun inventarioSinMutarElOriginal() {
        val base = mapOf("tornillos" to 10)
        assertEquals(10, cantidadDe(base, "tornillos"))
        assertEquals(0, cantidadDe(base, "tuercas"))
        assertEquals(mapOf("tornillos" to 15), agregar(base, "tornillos", 5))
        assertEquals(mapOf("tornillos" to 10, "tuercas" to 3), agregar(base, "tuercas", 3))
        assertEquals(mapOf("tornillos" to 10), base)
    }
}

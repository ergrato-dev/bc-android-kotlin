import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun tamanos() = assertEquals(listOf(1, 2, 4, 8), tamanosEnBytes())

    @Test
    fun divisiones() {
        assertEquals(3, divisionEntera(7, 2))
        assertEquals(-3, divisionEntera(-7, 2))
        assertEquals(3.5, divisionReal(7, 2))
    }

    @Test
    fun desbordamiento() {
        assertEquals(Int.MIN_VALUE, sumaConEnvoltura(Int.MAX_VALUE, 1))
        assertFailsWith<ArithmeticException> { sumaExacta(Int.MAX_VALUE, 1) }
        assertEquals(5, sumaExacta(2, 3))
    }

    @Test
    fun truncado() {
        assertEquals(3, truncar(3.7))
        assertEquals(-3, truncar(-3.7))
    }

    @Test
    fun comparacionDeDecimales() {
        assertFalse(0.1 + 0.2 == 0.3)
        assertTrue(casiIguales(0.1 + 0.2, 0.3))
        assertFalse(casiIguales(0.3, 0.31))
    }
}

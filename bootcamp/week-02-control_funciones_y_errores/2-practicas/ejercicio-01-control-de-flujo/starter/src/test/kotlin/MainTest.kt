import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun diasDeLaSemana() {
        assertEquals("Lunes", nombreDelDia(1))
        assertEquals("Domingo", nombreDelDia(7))
        assertEquals("Desconocido", nombreDelDia(0))
        assertEquals("Desconocido", nombreDelDia(8))
    }

    @Test
    fun fizzBuzzCubreLosCuatroCasos() {
        assertEquals("1", fizzBuzz(1))
        assertEquals("Fizz", fizzBuzz(9))
        assertEquals("Buzz", fizzBuzz(10))
        assertEquals("FizzBuzz", fizzBuzz(30))
    }

    @Test
    fun sumaDePares() {
        assertEquals(30, sumaPares(10))
        assertEquals(30, sumaPares(11))
        assertEquals(0, sumaPares(1))
        assertEquals(0, sumaPares(-5))
    }

    @Test
    fun cuentaAtrasVaDeMayorAMenor() {
        assertEquals(listOf(3, 2, 1), cuentaAtras(3))
        assertEquals(emptyList(), cuentaAtras(0))
    }

    @Test
    fun primos() {
        assertTrue(esPrimo(2))
        assertTrue(esPrimo(97))
        assertFalse(esPrimo(1))
        assertFalse(esPrimo(-7))
        assertFalse(esPrimo(91))
    }

    @Test
    fun primerMultiploMayorQue() {
        assertEquals(12, primerMultiplo(de = 4, mayorQue = 10))
        assertEquals(15, primerMultiplo(5, 10))
        assertNull(primerMultiplo(de = 7, mayorQue = 10, hasta = 13))
    }
}

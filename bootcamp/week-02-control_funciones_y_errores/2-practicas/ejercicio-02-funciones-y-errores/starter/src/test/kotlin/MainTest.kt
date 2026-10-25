import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun dividirYSuError() {
        assertEquals(2.5, dividir(5.0, 2.0))
        val e = assertFailsWith<IllegalArgumentException> { dividir(1.0, 0.0) }
        assertTrue(e.message.orEmpty().isNotBlank())
    }

    @Test
    fun parseaOdevuelveNull() {
        assertEquals(42, parseEntero(" 42 "))
        assertNull(parseEntero("4.2"))
        assertNull(parseEntero(""))
    }

    @Test
    fun parseaOLanzaConMensaje() {
        assertEquals(-7, parseEnteroOLanza("-7"))
        val e = assertFailsWith<IllegalArgumentException> { parseEnteroOLanza("abc") }
        assertEquals("No es un entero: 'abc'", e.message)
    }

    @Test
    fun promedioConVararg() {
        assertEquals(2.0, promedio(1.0, 2.0, 3.0))
        assertEquals(5.0, promedio(5.0))
        assertFailsWith<IllegalArgumentException> { promedio() }
    }

    @Test
    fun formatearConValoresPorDefectoYNombrados() {
        assertEquals("3.14", formatear(3.14159))
        assertEquals("    3.1416", formatear(3.14159, decimales = 4, ancho = 10))
        assertEquals("3", formatear(2.5, decimales = 0))
    }

    @Test
    fun leerSeguroYResultado() {
        assertEquals(10, leerSeguro("10"))
        assertEquals(-1, leerSeguro("x"))
        assertEquals(0, leerSeguro("x", porDefecto = 0))
        assertEquals("OK:42", resultado("42"))
        assertEquals("ERROR:No es un entero: 'abc'", resultado("abc"))
    }
}

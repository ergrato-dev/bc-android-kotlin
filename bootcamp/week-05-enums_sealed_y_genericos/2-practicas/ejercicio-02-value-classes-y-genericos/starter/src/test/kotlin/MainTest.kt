import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun valueClassesValidate() {
        assertEquals(7.5, (Meters(3.0) + Meters(4.5)).value)
        assertFailsWith<IllegalArgumentException> { Meters(-1.0) }
        assertEquals("a@b", Email("a@b").value)
        assertFailsWith<IllegalArgumentException> { Email("ab") }
        assertFailsWith<IllegalArgumentException> { Email("@b") }
        assertFailsWith<IllegalArgumentException> { Email("a@") }
        assertFailsWith<IllegalArgumentException> { Email("a@@b") }
    }

    @Test
    fun stackIsLastInFirstOut() {
        val s = Stack<String>()
        assertTrue(s.isEmpty())
        assertNull(s.popOrNull())
        s.push("a")
        s.push("b")
        assertEquals(2, s.size)
        assertEquals("b", s.peekOrNull())
        assertEquals("b", s.popOrNull())
        assertEquals("a", s.popOrNull())
        assertTrue(s.isEmpty())
    }

    @Test
    fun genericFunctionsKeepTheirType() {
        assertEquals("x", firstOrDefault(listOf("x", "y"), "z"))
        assertEquals("z", firstOrDefault(emptyList(), "z"))
        assertEquals(9, maxOfTwo(3, 9))
        assertEquals("pera", maxOfTwo("manzana", "pera"))
    }

    @Test
    fun clampBoundsAValue() {
        assertEquals(5, clamp(5, 0, 10))
        assertEquals(0, clamp(-3, 0, 10))
        assertEquals(10, clamp(99, 0, 10))
        assertEquals(1.5, clamp(1.5, 1.0, 2.0))
        assertFailsWith<IllegalArgumentException> { clamp(1, 5, 0) }
    }

    @Test
    fun covariantBoxesFitWhereAnyIsExpected() {
        val boxes: List<Box<Any>> = listOf(Box("a"), Box(1), Box(2.5))
        assertEquals(3, boxes.size)
        val strings: Box<String> = Box("hola")
        val asAny: Box<Any> = strings
        assertEquals("hola", asAny.value)
    }

    @Test
    fun outcomesMapOrPassTheErrorThrough() {
        assertEquals(Outcome.Ok(42), parseAge("42"))
        assertEquals(Outcome.Ok(43), parseAge("42").map { it + 1 })
        assertEquals(Outcome.Err("edad fuera de rango: 200"), parseAge("200"))
        assertEquals(Outcome.Err("no es un entero: 'abc'"), parseAge("abc"))
        assertEquals(Outcome.Err("no es un entero: 'abc'"), parseAge("abc").map { it + 1 })
    }

    @Test
    fun reifiedTypeChecks() {
        val mixed = listOf("a", 1, "b", 2.5, 3)
        assertEquals(2, mixed.countOfType<String>())
        assertEquals(2, mixed.countOfType<Int>())
        assertEquals(0, mixed.countOfType<Boolean>())
    }
}

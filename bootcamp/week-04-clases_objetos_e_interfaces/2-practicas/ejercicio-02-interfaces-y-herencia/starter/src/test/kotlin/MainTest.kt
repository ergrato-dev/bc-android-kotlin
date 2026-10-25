import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun extensions() {
        assertEquals("3.14", 3.14159.format2())
        assertEquals("2.50", 2.5.format2())
        assertTrue("ABC-123".isCode)
        assertFalse("abc-123".isCode)
        assertFalse("ABC-12".isCode)
        assertFalse("ABCD-123".isCode)
    }

    @Test
    fun shapesDescribeThemselves() {
        assertEquals("Círculo con área 78.54", Circle(5.0).describe())
        assertEquals("Cuadrado con área 9.00", Square(3.0).describe())
    }

    @Test
    fun functionsOverAnyShape() {
        val shapes = listOf(Square(2.0), Square(3.0), Circle(1.0))
        assertEquals(4.0 + 9.0 + Math.PI, totalArea(shapes), 1e-9)
        assertEquals(3.0, (largest(shapes) as Square).side)
        assertNull(largest(emptyList()))
    }

    @Test
    fun employeesHaveTheirOwnRules() {
        val m = Manager("Eva", 2000.0, 500.0)
        val d = Developer("Ana", 1000.0, 3)
        assertEquals(2500.0, m.salary(), 1e-9)
        assertEquals("Gerente", m.title())
        assertEquals(1300.0, d.salary(), 1e-9)
        assertEquals("Desarrollador nivel 3", d.title())
        assertFailsWith<IllegalArgumentException> { Developer("Bob", 1000.0, 6) }
        assertFailsWith<IllegalArgumentException> { Developer("Bob", 1000.0, 0) }
    }

    @Test
    fun payrollIsPolymorphic() {
        val staff: List<Employee> = listOf(Manager("Eva", 2000.0, 500.0), Developer("Ana", 1000.0, 3))
        assertEquals(3800.0, payroll(staff), 1e-9)
        assertEquals(0.0, payroll(emptyList()))
    }

    @Test
    fun versionParse() {
        assertEquals(Version(1, 2, 3), Version.parse("1.2.3"))
        assertNull(Version.parse("1.2"))
        assertNull(Version.parse("1.2.x"))
        assertNull(Version.parse("1.2.3.4"))
        assertNull(Version.parse("1.-2.3"))
        assertEquals("1.2.3", Version(1, 2, 3).toString())
    }

    @Test
    fun versionOrder() {
        val sorted = listOf(Version(1, 10, 0), Version(1, 2, 0), Version(0, 9, 9)).sorted()
        assertEquals(listOf("0.9.9", "1.2.0", "1.10.0"), sorted.map { it.toString() })
        assertTrue(Version(2, 0, 0) > Version(1, 99, 99))
    }
}

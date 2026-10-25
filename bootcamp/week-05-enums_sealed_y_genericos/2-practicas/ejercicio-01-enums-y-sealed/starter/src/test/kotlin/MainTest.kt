import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun weekdaysKnowTheWeekend() {
        assertTrue(Weekday.SATURDAY.isWeekend)
        assertTrue(Weekday.SUNDAY.isWeekend)
        assertFalse(Weekday.FRIDAY.isWeekend)
    }

    @Test
    fun weekdaysWrapAround() {
        assertEquals(Weekday.TUESDAY, Weekday.MONDAY.next())
        assertEquals(Weekday.MONDAY, Weekday.SUNDAY.next())
    }

    @Test
    fun priorityParseIsForgiving() {
        assertEquals(Priority.HIGH, Priority.parse("high"))
        assertEquals(Priority.LOW, Priority.parse("LOW"))
        assertNull(Priority.parse("urgente"))
        assertFailsWith<IllegalArgumentException> { Priority.valueOf("urgente") }
        assertEquals(10, Priority.HIGH.weight)
    }

    @Test
    fun countsKeepEveryKey() {
        assertEquals(
            mapOf(Priority.LOW to 2, Priority.MEDIUM to 0, Priority.HIGH to 1),
            countByPriority(listOf(Priority.LOW, Priority.HIGH, Priority.LOW)),
        )
        assertEquals(mapOf(Priority.LOW to 0, Priority.MEDIUM to 0, Priority.HIGH to 0), countByPriority(emptyList()))
        assertEquals("hoy", urgency(Priority.HIGH))
        assertEquals("esta semana", urgency(Priority.MEDIUM))
        assertEquals("puede esperar", urgency(Priority.LOW))
    }

    @Test
    fun shapesHaveAnArea() {
        assertEquals(12.0, area(Shape.Rect(3.0, 4.0)))
        assertEquals(Math.PI, area(Shape.Circle(1.0)), 1e-9)
        assertEquals(0.0, area(Shape.Empty))
    }

    @Test
    fun everyStateIsRendered() {
        assertEquals("Cargando...", render(LoadState.Loading))
        assertEquals("2 elementos", render(LoadState.Success(listOf("a", "b"))))
        assertEquals("Sin datos", render(LoadState.Success(emptyList())))
        assertEquals("Error: sin red", render(LoadState.Failure("sin red")))
    }
}

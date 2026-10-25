import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun counterIncrementsAndResets() {
        val c = Counter()
        assertTrue(c.isZero)
        c.increment()
        c.increment(5)
        assertEquals(6, c.value)
        assertFalse(c.isZero)
        c.reset()
        assertTrue(c.isZero)
        assertFailsWith<IllegalArgumentException> { c.increment(0) }
        assertEquals(0, c.value)
    }

    @Test
    fun temperatureConvertsAndValidates() {
        assertEquals(212.0, Temperature(100.0).fahrenheit, 1e-9)
        assertEquals(-40.0, Temperature(-40.0).fahrenheit, 1e-9)
        assertFailsWith<IllegalArgumentException> { Temperature(-300.0) }
        val t = Temperature(20.0)
        assertFailsWith<IllegalArgumentException> { t.celsius = -300.0 }
        assertEquals(20.0, t.celsius)
        t.celsius = 25.0
        assertEquals(25.0, t.celsius)
    }

    @Test
    fun rectangleDerivedProperties() {
        val r = Rectangle(3.0, 4.0)
        assertEquals(12.0, r.area)
        assertEquals(14.0, r.perimeter)
        assertFalse(r.isSquare)
        assertTrue(Rectangle(5.0).isSquare)
        assertFailsWith<IllegalArgumentException> { Rectangle(0.0, 2.0) }
        assertFailsWith<IllegalArgumentException> { Rectangle(-1.0) }
    }

    @Test
    fun pointsAddAndMeasure() {
        assertEquals(Point(4, 6), Point(1, 2) + Point(3, 4))
        assertEquals(5.0, Point(0, 0).distanceTo(Point(3, 4)), 1e-9)
        assertEquals(Point(1, 9), Point(1, 2).copy(y = 9))
        assertTrue(Point(1, 2) == Point(1, 2))
    }

    @Test
    fun playlistAddsAndRejects() {
        val p = Playlist()
        p.add(Song("Uno", 200))
        p.add(Song("Dos", 100))
        assertEquals(300, p.totalSeconds)
        assertEquals(listOf(Song("Uno", 200), Song("Dos", 100)), p.songs)
        assertFailsWith<IllegalArgumentException> { p.add(Song("Uno", 50)) }
        assertFailsWith<IllegalArgumentException> { p.add(Song("Tres", 0)) }
        assertEquals(2, p.songs.size)
    }

    @Test
    fun playlistSongsIsASnapshot() {
        val p = Playlist()
        p.add(Song("Uno", 10))
        val before = p.songs
        p.add(Song("Dos", 20))
        assertEquals(1, before.size)
        assertEquals(2, p.songs.size)
    }
}

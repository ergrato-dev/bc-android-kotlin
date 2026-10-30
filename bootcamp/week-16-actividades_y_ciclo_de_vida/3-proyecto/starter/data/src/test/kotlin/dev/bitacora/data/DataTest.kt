package dev.bitacora.data

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import kotlin.test.Test
import kotlin.test.assertEquals

class DataTest {
    @Test
    fun roundTrip() {
        val o = listOf(Observation("A-1", Level.HIGH, "riego"))
        assertEquals("""[{"code":"A-1","level":"HIGH","description":"riego"}]""", encode(o))
        assertEquals(o, decode(encode(o)))
    }
}

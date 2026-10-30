package dev.bitacora.data

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class DataTest {
    @Test
    fun roundTrip() {
        val o = listOf(Observation("A-1", Level.HIGH, "riego"))
        assertEquals("""[{"code":"A-1","level":"HIGH","description":"riego"}]""", encode(o))
        assertEquals(o, decode(encode(o)))
    }

    @Test
    fun placeAndPhotoRoundTrip() {
        val o = listOf(Observation("A-1", Level.HIGH, "riego", Position(-34.6037, -58.3816), "A-1.jpg"))
        assertEquals(
            """[{"code":"A-1","level":"HIGH","description":"riego","latitude":-34.6037,"longitude":-58.3816,"photo":"A-1.jpg"}]""",
            encode(o),
        )
        assertEquals(o, decode(encode(o)))
    }
}

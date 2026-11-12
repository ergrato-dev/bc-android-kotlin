package dev.bitacora.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoreTest {
    @Test
    fun counts() = assertEquals(mapOf(Level.LOW to 1, Level.MEDIUM to 0, Level.HIGH to 0), listOf(Observation("a", Level.LOW, "x")).countByLevel())

    @Test
    fun placeAndPhotoAreOptional() {
        val observation = Observation("A-1", Level.LOW, "riego")
        assertNull(observation.position)
        assertNull(observation.photo)
    }
}

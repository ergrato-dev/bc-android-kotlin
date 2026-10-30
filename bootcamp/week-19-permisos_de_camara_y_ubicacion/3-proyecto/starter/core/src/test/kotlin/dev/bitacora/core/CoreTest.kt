package dev.bitacora.core

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreTest {
    @Test
    fun counts() = assertEquals(mapOf(Level.LOW to 1, Level.MEDIUM to 0, Level.HIGH to 0), listOf(Observation("a", Level.LOW, "x")).countByLevel())
}

package dev.bitacora.core

import kotlin.test.Test
import kotlin.test.assertEquals

// El código de la siguiente es uno más que el mayor, y no depende de cuántas haya.
class CodeTest {
    private fun of(vararg codes: String) = codes.map { Observation(it, Level.LOW, "x") }

    @Test
    fun theFirstIsA1() = assertEquals("A-1", emptyList<Observation>().nextCode())

    @Test
    fun afterTheLast() = assertEquals("A-4", of("A-1", "A-2", "A-3").nextCode())

    // Borrada A-2: quedan dos, pero la siguiente no es A-3, que ya existe.
    @Test
    fun aGapIsNotReused() = assertEquals("A-4", of("A-1", "A-3").nextCode())
}

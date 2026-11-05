package dev.bitacora.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Lo que enseña el mapa: de la esquina suroeste a la noreste de las que tienen posición.
class AreaTest {
    private fun at(latitude: Double, longitude: Double) = Observation("A-1", Level.LOW, "x", Position(latitude, longitude))

    @Test
    fun withoutPositionsThereIsNoArea() = assertNull(listOf(Observation("A-1", Level.LOW, "x")).area())

    // Una sola: las dos esquinas son el mismo punto.
    @Test
    fun onePositionIsAPoint() = assertEquals(Position(-34.6, -58.4) to Position(-34.6, -58.4), listOf(at(-34.6, -58.4)).area())

    // Al sur y al oeste, los números más pequeños: -34.7 está más al sur que -34.5.
    @Test
    fun theCornersComeFromDifferentObservations() =
        assertEquals(Position(-34.7, -58.5) to Position(-34.5, -58.3), listOf(at(-34.5, -58.5), at(-34.7, -58.3)).area())

    @Test
    fun observationsWithoutPositionDoNotCount() =
        assertEquals(Position(1.0, 2.0) to Position(1.0, 2.0), listOf(at(1.0, 2.0), Observation("A-2", Level.HIGH, "y")).area())
}

package dev.bitacora

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// No los toques. Lo que se puede probar sin mapa: LatLng y LatLngBounds son clases de datos de Play Services, y
// funcionan en la JVM aunque no haya Play Services.
@RunWith(AndroidJUnit4::class)
class BoundsTest {
    @Test
    fun withoutBothCoordinatesThereIsNoPoint() {
        assertNull(Observation("A-2", Level.MEDIUM, "x").latLng())
        assertNull(Observation("A-4", Level.LOW, "x", latitude = -34.6).latLng())
    }

    @Test
    fun aPointIsItsCoordinates() = assertEquals(LatLng(-34.6037, -58.3816), observations.first().latLng())

    @Test
    fun withoutPositionsThereAreNoBounds() = assertNull(listOf(Observation("A-2", Level.MEDIUM, "x")).bounds())

    // Las esquinas salen de A-1 y A-3: la latitud y la longitud más bajas al suroeste, las más altas al noreste.
    @Test
    fun theBoundsHoldEveryPosition() {
        val bounds = observations.bounds()!!
        assertEquals(LatLng(-34.6118, -58.4173), bounds.southwest)
        assertEquals(LatLng(-34.6037, -58.3816), bounds.northeast)
        assertTrue(observations.mapNotNull { it.latLng() }.all(bounds::contains))
    }
}

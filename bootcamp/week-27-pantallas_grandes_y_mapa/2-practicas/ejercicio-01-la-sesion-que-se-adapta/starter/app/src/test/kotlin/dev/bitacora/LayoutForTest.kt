package dev.bitacora

import androidx.compose.material3.adaptive.Posture
import androidx.window.core.layout.WindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals

// No los toques. La decisión, sin pantalla: una WindowSizeClass y una Posture se construyen a mano.
class LayoutForTest {
    private val flat = Posture()

    // Un móvil en vertical: menos de 600 dp de ancho, compact.
    @Test
    fun aPhoneHasOnePane() = assertEquals(Layout.ONE_PANE, layoutFor(WindowSizeClass(minWidthDp = 0, minHeightDp = 480), flat))

    // 600 dp es medium: todavía un panel.
    @Test
    fun mediumHasOnePane() = assertEquals(Layout.ONE_PANE, layoutFor(WindowSizeClass(minWidthDp = 600, minHeightDp = 480), flat))

    @Test
    fun expandedHasTwoPanes() = assertEquals(Layout.TWO_PANES, layoutFor(WindowSizeClass(minWidthDp = 840, minHeightDp = 480), flat))

    // La postura gana al ancho: medio plegado sobre la mesa, la mesa, aunque la ventana sea expanded.
    @Test
    fun tabletopWinsOverTheWidth() =
        assertEquals(Layout.TABLETOP, layoutFor(WindowSizeClass(minWidthDp = 840, minHeightDp = 480), Posture(isTabletop = true)))
}

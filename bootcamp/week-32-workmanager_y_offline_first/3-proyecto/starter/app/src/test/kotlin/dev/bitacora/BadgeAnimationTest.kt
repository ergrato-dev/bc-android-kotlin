package dev.bitacora

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// La insignia cambia de color en 300 ms (teoría 01). Con autoAdvance = false, el reloj solo avanza cuando el test lo
// dice. El píxel (2, 20) es del fondo, no de la letra.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BadgeAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun pixel() = rule.onNodeWithTag("badge").captureToImage().toPixelMap()[2, 20]

    @Test
    fun theBadgeChangesColorIn300Milliseconds() {
        var level by mutableStateOf(Level.HIGH)
        rule.setContent { BitacoraTheme(dynamicColor = false) { LevelBadge(level, Modifier.testTag("badge")) } }
        val high = Color(context.getColor(R.color.level_high))
        val low = Color(context.getColor(R.color.level_low))
        assertEquals(high, pixel())
        rule.mainClock.autoAdvance = false
        level = Level.LOW
        // Sin avanzar el reloj, Compose recompone con el nivel nuevo: la animación empieza aquí.
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(150)
        assertNotEquals(high, pixel())
        assertNotEquals(low, pixel())
        rule.mainClock.advanceTimeBy(250)
        assertEquals(low, pixel())
    }
}

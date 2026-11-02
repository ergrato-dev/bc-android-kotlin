package dev.bitacora

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// A-1 es Alta, A-2 Media y A-3 Baja. Con rule.mainClock.autoAdvance = false, el reloj de Compose solo avanza cuando el
// test lo dice: así se ve la animación a medias (teoría 01).
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ObservationCardTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun color(id: Int) = Color(context.getColor(id))

    // El centro de la insignia de A-1, sin la letra.
    private fun badgeOfA1() = rule.onAllNodesWithTag("badge", useUnmergedTree = true)[0].captureToImage().toPixelMap()[4, 4]

    private fun topOfA2() = rule.onNodeWithText("A-2").fetchSemanticsNode().boundsInRoot.top

    @Test
    fun aTapOpensTheDescription() {
        launch()
        rule.onNodeWithText("A-1").performClick()
        rule.onNodeWithText("riego roto en el sector norte").assertExists()
    }

    @Test
    fun aSecondTapClosesIt() {
        launch()
        rule.onNodeWithText("A-1").performClick()
        rule.onNodeWithText("riego roto en el sector norte").assertExists()
        rule.onNodeWithText("A-1").performClick()
        rule.onNodeWithText("riego roto en el sector norte").assertDoesNotExist()
    }

    // Al abrir A-1, A-2 baja poco a poco: a los 50 ms está entre donde estaba y donde acaba.
    @Test
    fun theDescriptionGrowsIn() {
        launch()
        val closed = topOfA2()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("A-1").performClick()
        rule.mainClock.advanceTimeBy(50)
        val halfway = topOfA2()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        val open = topOfA2()
        assertTrue(halfway > closed && halfway < open, "$closed < $halfway < $open")
    }

    // Al cerrar, la descripción sigue en pantalla mientras se va.
    @Test
    fun itGoesOutSlowlyToo() {
        launch()
        rule.onNodeWithText("A-1").performClick()
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("A-1").performClick()
        rule.mainClock.advanceTimeBy(50)
        rule.onNodeWithText("riego roto en el sector norte").assertExists()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithText("riego roto en el sector norte").assertDoesNotExist()
    }

    // Alta, después Baja: ya no queda ninguna insignia con la A, y hay dos con la B (A-1 y A-3).
    @Test
    fun aLongPressChangesTheLevel() {
        launch()
        rule.onNodeWithText("A-1").performTouchInput { longClick() }
        rule.onAllNodesWithText("A").assertCountEquals(0)
        rule.onAllNodesWithText("B").assertCountEquals(2)
        rule.onNodeWithText("riego roto en el sector norte").assertDoesNotExist()
    }

    // TalkBack no hace pulsaciones largas: ofrece la acción, con su etiqueta.
    @Test
    fun theLongPressHasALabel() {
        launch()
        val action = rule.onNodeWithText("A-1").fetchSemanticsNode().config[SemanticsActions.OnLongClick]
        assertEquals("Cambiar el nivel", action.label)
    }

    @Test
    fun theBadgeHasTheLevelColor() {
        launch()
        assertEquals(color(R.color.level_high), badgeOfA1())
    }

    // tween(300): a los 150 ms, ni el de antes ni el de después; a los 400, el de Baja.
    @Test
    fun theBadgeChangesColorIn300Milliseconds() {
        launch()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("A-1").performTouchInput { longClick() }
        rule.mainClock.advanceTimeBy(150)
        val halfway = badgeOfA1()
        assertNotEquals(color(R.color.level_high), halfway)
        assertNotEquals(color(R.color.level_low), halfway)
        rule.mainClock.advanceTimeBy(250)
        assertEquals(color(R.color.level_low), badgeOfA1())
    }
}

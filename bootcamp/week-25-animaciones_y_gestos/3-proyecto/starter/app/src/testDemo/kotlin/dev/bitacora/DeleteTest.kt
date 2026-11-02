package dev.bitacora

import android.os.Bundle
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// La demo empieza con A-1 (Alta), A-2 (Media) y A-3 (Baja); en los 470 dp de Robolectric solo caben A-1 y A-2. Las
// filas se buscan por su código, como parte del texto.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class DeleteTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun row(code: String) = rule.onNodeWithText("$code ·", substring = true)

    private fun top(code: String) = row(code).fetchSemanticsNode().boundsInRoot.top

    // Lo que hace TalkBack: la acción "Borrar" de la fila.
    private fun deleteWithTalkBack(code: String) {
        val actions = row(code).fetchSemanticsNode().config[SemanticsActions.CustomActions]
        rule.runOnIdle { actions.single { it.label == "Borrar" }.action() }
    }

    @Test
    fun swipingLeftDeletesTheRow() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        row("A-2").assertDoesNotExist()
        rule.onNodeWithText("2 observaciones · Baja: 1 · Media: 0 · Alta: 1").assertExists()
    }

    @Test
    fun swipingRightDoesNothing() {
        launch()
        row("A-2").performTouchInput { swipeRight() }
        row("A-2").assertExists()
        rule.onNodeWithText("A-2 borrada").assertDoesNotExist()
    }

    @Test
    fun undoPutsItBackInItsPlace() {
        launch()
        row("A-1").performTouchInput { swipeLeft() }
        rule.onNodeWithText("A-1 borrada").assertExists()
        rule.onNodeWithText("Deshacer").performClick()
        assertTrue(top("A-1") < top("A-2"), "${top("A-1")} < ${top("A-2")}")
    }

    // SnackbarDuration.Long son 10 s.
    @Test
    fun theSnackbarGoesAwayAfterTenSeconds() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        rule.mainClock.advanceTimeBy(9_000)
        rule.onNodeWithText("Deshacer").assertExists()
        rule.mainClock.advanceTimeBy(2_000)
        rule.onNodeWithText("Deshacer").assertDoesNotExist()
        row("A-2").assertDoesNotExist()
    }

    // TalkBack lee la fila sin el "Borrar" de detrás, y la borra con su acción.
    @Test
    fun talkBackReadsTheRowAndCanDeleteIt() {
        launch()
        assertEquals(
            listOf("A", "A-1 · Alta · riego roto en el sector norte", "-34.6037, -58.3816"),
            row("A-1").fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.map { it.text },
        )
        rule.onAllNodesWithText("Borrar").assertCountEquals(0)
        deleteWithTalkBack("A-2")
        row("A-2").assertDoesNotExist()
        rule.onNodeWithText("A-2 borrada").assertExists()
    }

    // A los 50 ms de borrar A-1, A-2 está subiendo.
    @Test
    fun theRowsBelowMoveUpSlowly() {
        launch()
        val before = top("A-2")
        rule.mainClock.autoAdvance = false
        deleteWithTalkBack("A-1")
        rule.mainClock.advanceTimeBy(50)
        val halfway = top("A-2")
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        val after = top("A-2")
        assertTrue(halfway < before && halfway > after, "$after < $halfway < $before")
    }

    // Borrada A-2, quedan A-1 y A-3: la siguiente es A-4, no otra A-3.
    @Test
    fun aNewObservationNeverRepeatsACode() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        rule.onNodeWithText("Descripción").performTextInput("riego del sector 3")
        rule.onNodeWithText("Alta").performClick()
        rule.onNode(hasScrollAction()).performScrollToKey("A-4")
        row("A-4").assertExists()
        rule.onNode(hasScrollAction()).performScrollToKey("A-3")
        rule.onAllNodesWithText("A-3 ·", substring = true).assertCountEquals(1)
    }

    // Lo borrado sigue borrado tras la muerte del proceso, y se puede deshacer en la sesión nueva... si sale el aviso:
    // no sale, porque era de la pantalla de antes. La borrada sigue en el handle.
    @Test
    fun aDeletionSurvivesProcessDeath() {
        val first = launch()
        row("A-2").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup(saved)
        row("A-2").assertDoesNotExist()
        rule.onNodeWithText("2 observaciones · Baja: 1 · Media: 0 · Alta: 1").assertExists()
    }
}

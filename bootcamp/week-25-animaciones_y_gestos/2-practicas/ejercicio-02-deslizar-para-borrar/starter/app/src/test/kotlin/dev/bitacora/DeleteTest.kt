package dev.bitacora

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// La sesión empieza con A-1, A-2 y A-3. Las filas se buscan por su código, como parte del texto.
@RunWith(AndroidJUnit4::class)
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
        row("A-1").assertExists()
        row("A-3").assertExists()
    }

    // Hacia la derecha no borra: un gesto, una acción.
    @Test
    fun swipingRightDoesNothing() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        row("A-3").performTouchInput { swipeRight() }
        row("A-2").assertDoesNotExist()
        row("A-3").assertExists()
    }

    @Test
    fun aSnackbarOffersToUndo() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        rule.onNodeWithText("A-2 borrada").assertExists()
        rule.onNodeWithText("Deshacer").assertExists()
    }

    // Vuelve a su sitio, entre A-1 y A-3.
    @Test
    fun undoPutsItBackInItsPlace() {
        launch()
        row("A-2").performTouchInput { swipeLeft() }
        rule.onNodeWithText("Deshacer").performClick()
        assertTrue(top("A-1") < top("A-2") && top("A-2") < top("A-3"), "${top("A-1")} < ${top("A-2")} < ${top("A-3")}")
    }

    // SnackbarDuration.Long son 10 s. Con una acción y sin duración, no se iría nunca.
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

    @Test
    fun talkBackCanDeleteToo() {
        launch()
        deleteWithTalkBack("A-2")
        row("A-2").assertDoesNotExist()
        rule.onNodeWithText("A-2 borrada").assertExists()
    }

    // TalkBack lee la fila entera, sin el "Borrar" de detrás.
    @Test
    fun talkBackReadsOnlyTheRow() {
        launch()
        assertEquals(listOf("A-1 · Alta · riego roto en el sector norte"), row("A-1").fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)?.map { it.text })
        rule.onAllNodesWithText("Borrar").assertCountEquals(0)
        assertEquals(listOf("Borrar"), row("A-1").fetchSemanticsNode().config.getOrNull(SemanticsActions.CustomActions)?.map { it.label })
    }

    // A los 50 ms de borrar A-2, A-3 está subiendo: entre donde estaba y donde acaba.
    @Test
    fun theRowsBelowMoveUpSlowly() {
        launch()
        val before = top("A-3")
        rule.mainClock.autoAdvance = false
        deleteWithTalkBack("A-2")
        rule.mainClock.advanceTimeBy(50)
        val halfway = top("A-3")
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        val after = top("A-3")
        assertTrue(halfway < before && halfway > after, "$after < $halfway < $before")
    }
}

package dev.bitacora

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

// Recompositions cuenta cada composición: la pantalla como "screen" y cada fila con su código. Los tests miran cuántas
// hay después de empezar, no las de la primera vez. En los 470 dp de Robolectric se ven unas cinco filas.
@RunWith(AndroidJUnit4::class)
class RecompositionTest {
    @get:Rule
    val rule = createComposeRule()

    private val session = Session()

    @Before
    fun screen() {
        Recompositions.counts.clear()
        rule.setContent {
            val observations by session.observations.collectAsState()
            val draft by session.draft.collectAsState()
            BitacoraTheme { SessionScreen(observations, draft, session::onDraftChange, session::add, session::nextLevel) }
        }
        rule.waitForIdle()
        Recompositions.counts.clear()
    }

    private fun count(name: String) = Recompositions.counts[name] ?: 0

    private val visible = listOf("A-1", "A-2", "A-3", "A-4")

    // Cada tecla recompone la pantalla, que recibe el borrador; las filas no han cambiado.
    @Test
    fun typingDoesNotRecomposeTheRows() {
        repeat(5) { rule.onNodeWithText("Descripción").performTextInput("r") }
        assertEquals(listOf(0, 0, 0, 0), visible.map(::count))
    }

    // A-1 pasa de Baja a Media: solo cambia su fila.
    @Test
    fun aNewLevelRecomposesOnlyItsRow() {
        rule.onNodeWithText("A-1 ·", substring = true).performClick()
        rule.onNodeWithText("A-1 · Media · plaga 1").assertExists()
        assertEquals(listOf(1, 0, 0, 0), visible.map(::count))
    }

    // La nueva va al final, y no se ve: ninguna fila de las que se ven se recompone.
    @Test
    fun addingRecomposesNoRowOnScreen() {
        rule.onNodeWithText("Descripción").performTextInput("helada")
        rule.onNodeWithText("Añadir").performClick()
        assertEquals(101, session.observations.value.size)
        assertEquals(listOf(0, 0, 0, 0), visible.map(::count))
    }

    // Al bajar salen varias filas por arriba; la pantalla solo tiene que cambiar una vez, cuando aparece "Arriba".
    @Test
    fun scrollingRecomposesTheScreenOnce() {
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.onNodeWithText("Arriba").assertExists()
        assertEquals(1, count("screen"))
    }

    @Test
    fun upIsHiddenAtTheTop() {
        rule.onNodeWithText("Arriba").assertDoesNotExist()
    }

    @Test
    fun upGoesBackToTheTop() {
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.onNodeWithText("A-1 ·", substring = true).assertDoesNotExist()
        rule.onNodeWithText("Arriba").performClick()
        rule.onNodeWithText("A-1 · Baja · plaga 1").assertExists()
        rule.onNodeWithText("Arriba").assertDoesNotExist()
    }

    @Test
    fun aRowShowsItsTags() {
        rule.onNodeWithText("A-2 · Media · plaga 2").assertExists()
        rule.onNodeWithText("#campo #sector 2").assertExists()
    }

    // Añadir vacía el borrador: la pantalla se recompone, pero las filas no.
    @Test
    fun anEmptyDraftAddsNothing() {
        rule.onNodeWithText("Añadir").performClick()
        assertEquals(100, session.observations.value.size)
        assertEquals(listOf(0, 0, 0, 0), visible.map(::count))
    }
}

package dev.bitacora

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

// Ya está hecho: el enunciado. No lo toques.
@RunWith(AndroidJUnit4::class)
class UiTest {
    @get:Rule
    val rule = createComposeRule()

    private var value = 0

    private fun rating(start: Int = 3) {
        rule.setContent {
            var state by remember { mutableIntStateOf(start) }
            Rating("Estado de la parcela", state, {
                state = it
                value = it
            })
        }
    }

    // La valoración

    @Test
    fun aTapOnTheFourthCircleGivesFour() {
        rating()
        // 200 dp de ancho, densidad 1 en Robolectric: el cuarto círculo va de 120 a 160.
        rule.onNodeWithTag("rating").performTouchInput { click(Offset(140f, 20f)) }
        assertEquals(4, value)
    }

    @Test
    fun itSaysWhatItRates() {
        rating()
        rule.onNodeWithContentDescription("Estado de la parcela").assertExists()
    }

    @Test
    fun itSaysWhereItIs() {
        rating()
        rule.onNodeWithTag("rating").assert(hasStateDescription("3 de 5"))
    }

    @Test
    fun itIsARangeFromOneToFive() {
        rating()
        rule.onNodeWithTag("rating")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(3f, 1f..5f, 3)))
    }

    @Test
    fun talkBackCanChangeIt() {
        rating()
        rule.onNodeWithTag("rating").performSemanticsAction(SemanticsActions.SetProgress) { it(4.6f) }
        assertEquals(5, value)
        rule.onNodeWithTag("rating").assert(hasStateDescription("5 de 5"))
    }

    @Test
    fun itStaysBetweenOneAndFive() {
        rating()
        rule.onNodeWithTag("rating").performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        assertEquals(1, value)
    }

    // La nota que se guarda sola. Con el reloj parado, nada pasa hasta que el test lo mueve. La tecla cambia el estado,
    // pero la recomposición (y el LaunchedEffect que lanza) llega en el fotograma siguiente: advanceTimeByFrame().

    private val saves = mutableListOf<String>()

    private fun note() {
        rule.mainClock.autoAdvance = false
        rule.setContent { NoteField(onSave = { saves += it }) }
    }

    private fun type(text: String) {
        rule.onNodeWithText("Nota").performTextInput(text)
        rule.mainClock.advanceTimeByFrame()
    }

    @Test
    fun nothingIsSavedBeforeASecond() {
        note()
        type("riego")
        rule.mainClock.advanceTimeBy(SAVE_AFTER - 1, ignoreFrameDuration = true)
        assertEquals(emptyList(), saves)
        rule.onNodeWithText("Guardando…").assertExists()
    }

    @Test
    fun itSavesAfterASecond() {
        note()
        type("riego")
        rule.mainClock.advanceTimeBy(SAVE_AFTER, ignoreFrameDuration = true)
        assertEquals(listOf("riego"), saves)
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("Guardado").assertExists()
    }

    @Test
    fun typingAgainStartsTheWaitAgain() {
        note()
        type("rie")
        rule.mainClock.advanceTimeBy(600, ignoreFrameDuration = true)
        type("go")
        rule.mainClock.advanceTimeBy(600, ignoreFrameDuration = true)
        assertEquals(emptyList(), saves)
        rule.mainClock.advanceTimeBy(400, ignoreFrameDuration = true)
        assertEquals(listOf("riego"), saves)
    }

    // Con el reloj en marcha, waitForIdle no espera a un delay; waitUntil sí: mueve el reloj hasta que se cumple.
    @Test
    fun withTheClockRunningWaitUntilWaits() {
        rule.setContent { NoteField(onSave = { saves += it }) }
        rule.onNodeWithText("Nota").performTextInput("riego")
        rule.waitUntil(timeoutMillis = 3_000) { saves.isNotEmpty() }
        rule.onNodeWithText("Guardado").assertExists()
    }

    // Lo que llega de otro hilo

    @Test
    fun thePlotsArrive() {
        rule.setContent {
            Plots(load = {
                Thread.sleep(300)
                listOf("Parcela 1", "Parcela 2")
            })
        }
        rule.waitUntil(timeoutMillis = 2_000) {
            rule.onAllNodesWithText("Parcela 1").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Parcela 2").assertExists()
    }
}

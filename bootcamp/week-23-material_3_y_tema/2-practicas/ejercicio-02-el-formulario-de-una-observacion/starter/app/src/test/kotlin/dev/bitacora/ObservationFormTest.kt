package dev.bitacora

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

// El formulario solo, con su estado en el test, como lo tendría quien lo usa. Las cuentas, en el comentario de cada test.
@RunWith(AndroidJUnit4::class)
class ObservationFormTest {
    @get:Rule
    val rule = createComposeRule()

    private var draft by mutableStateOf("")
    private var level by mutableStateOf<Level?>(null)
    private var saved = 0

    private fun form(draft: String = "", level: Level? = null) {
        this.draft = draft
        this.level = level
        rule.setContent {
            BitacoraTheme(dynamicColor = false) {
                ObservationForm(this.draft, this.level, { this.draft = it }, { this.level = it }, { saved++ })
            }
        }
    }

    @Test
    fun theCounterCounts() {
        form()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("5 / 80").assertExists()
    }

    // 80 vale; 81 es un error, y el campo lo dice con su semántica: lo lee TalkBack.
    @Test
    fun tooLongIsAnError() {
        form("a".repeat(80), Level.HIGH)
        rule.onNodeWithText("80 / 80").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
        rule.onNodeWithText("Descripción").performTextInput("a")
        rule.onNodeWithText("81 / 80").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
    }

    @Test
    fun savingNeedsADescriptionAndALevel() {
        form()
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
        rule.onNodeWithText("Media").performClick()
        rule.onNodeWithText("Guardar").assertIsEnabled().performClick()
        assertEquals(1, saved)
    }

    // Solo espacios no es una descripción.
    @Test
    fun blankIsNotADescription() {
        form("   ", Level.LOW)
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
    }

    // Los niveles son una sola elección: botones de opción, no botones, con uno marcado.
    @Test
    fun theLevelsAreOneChoice() {
        form(level = Level.MEDIUM)
        rule.onNodeWithText("Media").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).assertIsSelected()
        rule.onNodeWithText("Alta").assertIsNotSelected().performClick()
        assertEquals(Level.HIGH, level)
        rule.onNodeWithText("Alta").assertIsSelected()
    }

    // "Hecho" en el teclado guarda, como el botón...
    @Test
    fun doneOnTheKeyboardSaves() {
        form("riego", Level.LOW)
        rule.onNodeWithText("Descripción").performImeAction()
        assertEquals(1, saved)
    }

    // ...pero solo si el botón se podría pulsar.
    @Test
    fun doneDoesNotSaveWhatCannotBeSaved() {
        form("riego")
        rule.onNodeWithText("Descripción").performImeAction()
        assertEquals(0, saved)
    }

    // titleLarge, en la escala de Material: 22 sp.
    @Test
    fun theTitleIsATitle() {
        form()
        val layout = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText("Nueva observación").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
        assertEquals(22.sp, layout.single().layoutInput.style.fontSize)
    }
}

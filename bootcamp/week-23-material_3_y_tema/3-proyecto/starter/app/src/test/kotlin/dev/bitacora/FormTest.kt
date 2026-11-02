package dev.bitacora

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.MAX_DESCRIPTION
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertNull
import kotlin.test.assertEquals

// Semana 23: el formulario. Vale para los dos sabores.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class FormTest {
    @get:Rule
    val rule = createComposeRule()

    private fun screen(draft: String) = rule.setContent {
        BitacoraTheme(dynamicColor = false) {
            BitacoraScreen("Bitácora agro v1.0.0", BitacoraUiState(draft = draft), {}, {}, {}, {}, {})
        }
    }

    // La cuenta solo aparece cuando sobra: con 80 no se ve.
    @Test
    fun theCountOnlyShowsWhenTooLong() {
        screen("a".repeat(80))
        rule.onNodeWithText("80 / 80").assertDoesNotExist()
        rule.onNodeWithText("Alta").assertIsEnabled()
    }

    // Sin descripción no hay nada que añadir: los niveles no se pueden pulsar.
    @Test
    fun aBlankDraftDisablesTheLevels() {
        screen("  ")
        for (level in listOf("Baja", "Media", "Alta")) rule.onNodeWithText(level).assertIsNotEnabled()
    }

    @Test
    fun aDraftEnablesThem() {
        screen("riego")
        for (level in listOf("Baja", "Media", "Alta")) rule.onNodeWithText(level).assertIsEnabled()
    }

    // 80 vale; 81, no: el campo lo marca como error y los niveles no se pueden pulsar.
    @Test
    fun tooLongIsAnError() {
        screen("a".repeat(81))
        rule.onNodeWithText("81 / 80").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        rule.onNodeWithText("Alta").assertIsNotEnabled()
    }

    // El título con el estilo de título del tema: 22 sp.
    @Test
    fun theTitleIsATitle() {
        screen("")
        val layout = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText("Bitácora agro v1.0.0").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
        assertEquals(22.sp, layout.single().layoutInput.style.fontSize)
    }

    // La pantalla no es la única que lo comprueba: el ViewModel tampoco guarda una descripción demasiado larga.
    @Test
    fun theViewModelRefusesTooLong() {
        val notes = ObservationDraftViewModel(SavedStateHandle(mapOf("draft" to "a".repeat(MAX_DESCRIPTION + 1))))
        assertNull(notes.add(Level.LOW))
    }
}

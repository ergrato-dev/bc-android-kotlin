package dev.bitacora

import android.os.Bundle
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

// Ya está hecho: createComposeRule pinta un composable suelto en una actividad vacía (la de ui-test-manifest).
// Los nodos se buscan por su texto, como los ve el usuario.
@RunWith(AndroidJUnit4::class)
class DraftTest {
    @get:Rule
    val rule = createComposeRule()

    private fun typeAndChoose(text: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(text)
        rule.onNodeWithText(level).performClick()
    }

    @Test
    fun thePickerShowsTheSelectedLevel() {
        rule.setContent { LevelPicker(selected = Level.MEDIUM, onSelect = {}) }
        rule.onNodeWithText("Media").assertIsSelected()
        rule.onNodeWithText("Baja").assertIsNotSelected()
        rule.onNodeWithText("Alta").assertIsNotSelected()
    }

    @Test
    fun thePickerTellsWhatWasChosen() {
        var chosen: Level? = null
        rule.setContent { LevelPicker(selected = null, onSelect = { chosen = it }) }
        rule.onNodeWithText("Alta").performClick()
        assertEquals(Level.HIGH, chosen)
    }

    // Sin estado: si quien lo usa no cambia selected, sigue marcado el mismo.
    @Test
    fun thePickerDoesNotChangeOnItsOwn() {
        rule.setContent { LevelPicker(selected = Level.MEDIUM, onSelect = {}) }
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("Media").assertIsSelected()
        rule.onNodeWithText("Alta").assertIsNotSelected()
    }

    @Test
    fun typingCountsTheCharacters() {
        rule.setContent { DraftScreen() }
        rule.onNodeWithText("0/80").assertExists()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("5/80").assertExists()
    }

    @Test
    fun aLongTextIsCut() {
        rule.setContent { DraftScreen() }
        rule.onNodeWithText("Descripción").performTextInput("a".repeat(100))
        rule.onNodeWithText("80/80").assertExists()
    }

    @Test
    fun savingNeedsATextAndALevel() {
        rule.setContent { DraftScreen() }
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("Guardar").assertIsEnabled()
    }

    @Test
    fun savingCountsAndClearsTheDraft() {
        rule.setContent { DraftScreen() }
        typeAndChoose("riego", "Alta")
        rule.onNodeWithText("Guardar").performClick()
        rule.onNodeWithText("Guardadas: 1").assertExists()
        rule.onNodeWithText("0/80").assertExists()
        rule.onNodeWithText("Alta").assertIsNotSelected()
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
    }

    // Un giro: la actividad se destruye y su estado viaja en un Bundle. El tester lo simula sin actividad nueva.
    @Test
    fun theDraftSurvivesARotation() {
        val tester = StateRestorationTester(rule)
        tester.setContent { DraftScreen() }
        typeAndChoose("riego", "Alta")
        rule.onNodeWithText("Guardar").performClick()
        typeAndChoose("plaga", "Media")
        tester.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("plaga").assertExists()
        rule.onNodeWithText("5/80").assertExists()
        rule.onNodeWithText("Media").assertIsSelected()
        rule.onNodeWithText("Guardadas: 1").assertExists()
    }
}

// Ya está hecho: la actividad de verdad, muerta y recreada con su Bundle, como cuando Android mata el proceso.
// createEmptyComposeRule no pinta nada: encuentra lo que pinten las actividades del test.
@RunWith(AndroidJUnit4::class)
class ProcessDeathTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    @Test
    fun theDraftSurvivesProcessDeath() {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Descripción").performTextInput("plaga")
        rule.onNodeWithText("Baja").performClick()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup(saved)
        rule.onNodeWithText("plaga").assertExists()
        rule.onNodeWithText("Baja").assertIsSelected()
    }
}

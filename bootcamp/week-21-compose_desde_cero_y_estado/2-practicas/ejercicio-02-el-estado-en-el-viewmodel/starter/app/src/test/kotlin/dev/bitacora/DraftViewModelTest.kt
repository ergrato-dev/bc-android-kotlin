package dev.bitacora

import android.os.Bundle
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Ya está hecho: la pantalla sin estado, sola. Pinta lo que le dan y avisa de lo que pasa; no cambia nada.
@RunWith(AndroidJUnit4::class)
class DraftScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun itPaintsTheState() {
        rule.setContent { DraftScreen(DraftUiState("riego", Level.HIGH, listOf("plaga", "helada")), {}, {}, {}) }
        rule.onNodeWithText("5/80").assertExists()
        rule.onNodeWithText("Alta").assertIsSelected()
        rule.onNodeWithText("Guardar").assertIsEnabled()
        rule.onNodeWithText("Guardadas: 2").assertExists()
        rule.onNodeWithText("plaga").assertExists()
        rule.onNodeWithText("helada").assertExists()
    }

    @Test
    fun itOnlySendsEvents() {
        val events = mutableListOf<Any>()
        rule.setContent { DraftScreen(DraftUiState(), { events += it }, { events += it }, { events += "save" }) }
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Media").performClick()
        assertEquals(listOf<Any>("riego", Level.MEDIUM), events)
        rule.onNodeWithText("0/80").assertExists()
        rule.onNodeWithText("Guardar").assertIsNotEnabled()
    }
}

// Ya está hecho: la actividad entera, con su ViewModel.
@RunWith(AndroidJUnit4::class)
class DraftRouteTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun viewModel() = ViewModelProvider(rule.activity)[DraftViewModel::class.java]

    private fun typeAndChoose(text: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(text)
        rule.onNodeWithText(level).performClick()
    }

    @Test
    fun typingGoesThroughTheViewModel() {
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("5/80").assertExists()
        assertEquals("riego", viewModel().state.value.text)
    }

    @Test
    fun savingAddsTheDescriptionToTheList() {
        typeAndChoose("  riego  ", "Alta")
        rule.onNodeWithText("Guardar").performClick()
        rule.onNodeWithText("Guardadas: 1").assertExists()
        rule.onNodeWithText("0/80").assertExists()
        assertEquals(listOf("riego"), viewModel().state.value.saved)
    }

    // Un giro destruye la actividad, pero no su ViewModel.
    @Test
    fun theViewModelSurvivesARotation() {
        val before = viewModel()
        typeAndChoose("plaga", "Baja")
        rule.activityRule.scenario.recreate()
        assertSame(before, viewModel())
        rule.onNodeWithText("plaga").assertExists()
        rule.onNodeWithText("Baja").assertIsSelected()
    }
}

@RunWith(AndroidJUnit4::class)
class DraftProcessDeathTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    // La muerte del proceso sí se lleva el ViewModel: lo que vuelve es lo que había en el SavedStateHandle.
    @Test
    fun theDraftSurvivesProcessDeath() {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("Guardar").performClick()
        rule.onNodeWithText("Descripción").performTextInput("plaga")
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup(saved)
        rule.onNodeWithText("plaga").assertExists()
        rule.onNodeWithText("Guardadas: 1").assertExists()
    }
}

// Ya está hecho: el ViewModel con un SavedStateHandle hecho a mano, como el que llega tras la muerte del proceso.
@RunWith(AndroidJUnit4::class)
class DraftViewModelTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun aRestoredHandleRestoresTheScreen() {
        val viewModel = DraftViewModel(SavedStateHandle(mapOf("text" to "plaga", "level" to Level.LOW, "saved" to arrayListOf("riego"))))
        rule.setContent { DraftRoute(viewModel) }
        rule.onNodeWithText("5/80").assertExists()
        rule.onNodeWithText("Baja").assertIsSelected()
        rule.onNodeWithText("Guardadas: 1").assertExists()
    }

    // Sin pantalla: las reglas del borrador se ven en el handle.
    @Test
    fun theTextIsCut() {
        val handle = SavedStateHandle()
        DraftViewModel(handle).onTextChange("a".repeat(100))
        assertEquals(80, handle.get<String>("text")?.length)
    }

    @Test
    fun onlyACompleteDraftIsSaved() {
        val handle = SavedStateHandle()
        val viewModel = DraftViewModel(handle)
        viewModel.onTextChange("riego")
        viewModel.save()
        assertEquals(emptyList(), handle.get<List<String>>("saved").orEmpty())
        viewModel.onTextChange("   ")
        viewModel.onLevelChange(Level.LOW)
        viewModel.save()
        assertEquals(emptyList(), handle.get<List<String>>("saved").orEmpty())
        viewModel.onTextChange("riego")
        viewModel.save()
        assertEquals(listOf("riego"), handle.get<List<String>>("saved"))
        assertEquals("", handle.get<String>("text"))
        assertEquals(null, handle.get<Level>("level"))
    }
}

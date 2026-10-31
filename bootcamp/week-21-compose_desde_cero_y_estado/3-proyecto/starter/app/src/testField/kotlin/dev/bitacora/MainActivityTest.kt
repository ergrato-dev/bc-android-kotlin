package dev.bitacora

import android.os.Bundle
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

// El móvil de Robolectric está en inglés de EE. UU.: la clase fija el idioma por defecto de la app, el español.
// Semana 21: la pantalla es de Compose. createEmptyComposeRule encuentra lo que pinten las actividades del test, y los
// nodos se buscan por su texto, como los ve el usuario.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class MainActivityTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun add(description: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    private fun oneObservationAndADraft() {
        add("riego del sector 3", "Alta")
        rule.onNodeWithText("Descripción").performTextInput("plaga en la parcela 2")
    }

    private fun assertOneObservationAndADraft() {
        rule.onNodeWithText("plaga en la parcela 2").assertExists()
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").assertExists()
        rule.onNodeWithText("1 observación · Baja: 0 · Media: 0 · Alta: 1").assertExists()
    }

    @Test
    fun theTitleHasTheDomainAndTheVersion() {
        assertEquals("Bitácora agro v1.0.0", Robolectric.buildActivity(MainActivity::class.java).setup().get().title)
        rule.onNodeWithText("Bitácora agro v1.0.0").assertExists()
    }

    @Test
    fun addingNumbersTheObservationsAndClearsTheDraft() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        add("riego", "Baja")
        add("plaga", "Alta")
        rule.onNodeWithText("A-1 · Baja · riego").assertExists()
        rule.onNodeWithText("A-2 · Alta · plaga").assertExists()
        rule.onNodeWithText("plaga").assertDoesNotExist()
    }

    @Test
    fun anEmptyDraftAddsNothing() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        add("   ", "Media")
        rule.onNodeWithText("0 observaciones · Baja: 0 · Media: 0 · Alta: 0").assertExists()
    }

    @Test
    fun everythingSurvivesARotation() {
        ActivityScenario.launch(MainActivity::class.java).use {
            oneObservationAndADraft()
            RuntimeEnvironment.setQualifiers("+land")
            assertOneObservationAndADraft()
        }
    }

    @Test
    fun everythingSurvivesProcessDeath() {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        oneObservationAndADraft()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup(saved)
        assertOneObservationAndADraft()
    }
}

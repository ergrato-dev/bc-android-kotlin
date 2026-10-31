package dev.bitacora

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class LanguagesTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    // Los botones de la pantalla, de arriba abajo: los nodos con el papel de botón, con su texto.
    private fun buttons() = rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        .fetchSemanticsNodes().map { it.config[SemanticsProperties.Text].joinToString() }

    private fun add(field: String, description: String, level: String) {
        rule.onNodeWithText(field).performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    @Test
    @Config(qualifiers = "es")
    fun theScreenSpeaksSpanish() {
        launch()
        assertEquals(listOf("Ubicar", "Baja", "Media", "Alta", "Compartir"), buttons())
        rule.onNodeWithText("Descripción").assertExists()
    }

    @Test
    @Config(qualifiers = "es")
    fun theSummaryUsesThePlural() {
        launch()
        add("Descripción", "riego", "Baja")
        add("Descripción", "plaga", "Baja")
        rule.onNodeWithText("2 observaciones · Baja: 2 · Media: 0 · Alta: 0").assertExists()
    }

    @Test
    @Config(qualifiers = "en")
    fun theScreenIsTranslatedToEnglish() {
        assertEquals("Field log agro v1.0.0", launch().title)
        assertEquals(listOf("Locate", "Low", "Medium", "High", "Share"), buttons())
        add("Description", "irrigation", "Medium")
        rule.onNodeWithText("1 observation · Low: 0 · Medium: 1 · High: 0").assertExists()
    }

    @Test
    @Config(qualifiers = "fr")
    fun aLanguageWeDoNotHaveFallsBackToSpanish() {
        assertEquals("Bitácora agro v1.0.0", launch().title)
    }
}

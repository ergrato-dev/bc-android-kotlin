package dev.bitacora.map

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.model.Position
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Semana 33: el mapa se prueba en su módulo, sin la app: sus textos, en español, salen de su propia R.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class MapScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val observations = listOf(
        Observation("A-1", Level.HIGH, "roya en el lote 2", Position(4.6, -74.08)),
        Observation("A-2", Level.LOW, "riego"),
    )

    // El título es un encabezado: TalkBack salta a él.
    @Test
    fun theTitleIsAHeading() {
        rule.setContent { MapScreen(observations, snippet = { it.description }, onOpen = {}) }
        rule.onNodeWithText("Mapa").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    // Una de las dos tiene lugar: «1 de 2».
    @Test
    fun itCountsTheOnesWithAPlace() {
        rule.setContent { MapScreen(observations, snippet = { it.description }, onOpen = {}) }
        rule.onNodeWithText("1 de 2 en el mapa").assertIsDisplayed()
    }
}

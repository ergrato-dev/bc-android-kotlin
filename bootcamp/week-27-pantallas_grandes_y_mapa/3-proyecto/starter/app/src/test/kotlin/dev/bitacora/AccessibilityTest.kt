package dev.bitacora

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Lo que lee TalkBack, mirado en el árbol de semántica unido: es el que se convierte en lo que recibe el lector de
// pantalla. Vale para los dos sabores.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class AccessibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private val riego = Observation("A-1", Level.HIGH, "riego roto", Position(-34.6037, -58.3816))

    private fun session(observations: List<Observation> = listOf(riego)) = rule.setContent {
        BitacoraTheme { BitacoraScreen("Bitácora agro v1.0.0", BitacoraUiState(observations = observations), {}, {}, {}, {}, {}) }
    }

    private fun texts(node: SemanticsNode) = node.config.getOrNull(SemanticsProperties.Text)?.map { it.text }

    // La insignia repite el nivel, que ya está en la línea: TalkBack leía "A, A-1 · Alta · riego roto...".
    @Test
    fun aRowIsReadWithoutItsBadge() {
        session()
        val row = rule.onNodeWithText("A-1 ·", substring = true).fetchSemanticsNode()
        assertEquals(listOf("A-1 · Alta · riego roto", "-34.6037, -58.3816"), texts(row))
    }

    // Con el gesto de encabezados, TalkBack salta al título.
    @Test
    fun theTitleIsAHeading() {
        session()
        rule.onNodeWithText("Bitácora agro v1.0.0").assert(isHeading())
    }

    // El resumen cambia al añadir o borrar: TalkBack lo lee sin que nadie lo enfoque, cuando termina lo que estaba
    // leyendo.
    @Test
    fun theSummaryIsAPoliteLiveRegion() {
        session()
        rule.onNodeWithText("1 observación", substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    // En el detalle, el código es el encabezado, y el botón del nivel que tiene dice que está seleccionado.
    @Test
    fun theDetailSaysWhichLevelItHas() {
        rule.setContent { BitacoraTheme { ObservationScreen(riego, onChoose = {}) } }
        rule.onNodeWithText("A-1").assert(isHeading())
        rule.onNodeWithText("Alta").assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        rule.onNodeWithText("Media").assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        rule.onNodeWithText("Baja").assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
    }

    // Todo lo que se pulsa dice qué es y mide al menos 48 x 48 dp para el dedo. En Robolectric la densidad es 1: un dp
    // es un píxel.
    @Test
    fun everythingClickableHasALabelAndRoomForAFinger() {
        session(listOf(riego, Observation("A-2", Level.LOW, "helada")))
        val clickable = rule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertEquals(9, clickable.size, "el campo, Ubicar, los tres niveles, las dos filas, Compartir y Mapa")
        for (node in clickable) {
            val label = texts(node).orEmpty() + node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            assertTrue(label.isNotEmpty(), "sin etiqueta: ${node.config}")
            val touch = node.touchBoundsInRoot
            assertTrue(touch.width >= 48.dp.value && touch.height >= 48.dp.value, "$label: ${touch.size}")
        }
    }
}

package dev.bitacora

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Todo se mira en el árbol de semántica unido, el que llega a TalkBack: un nodo es una parada del lector de pantalla.
@RunWith(AndroidJUnit4::class)
class ObservationCardTest {
    @get:Rule
    val rule = createComposeRule()

    // La tarjeta con su estado: seguir y urgente cambian de verdad.
    private fun card(description: String = "riego roto en el sector norte") = rule.setContent {
        var observation by remember { mutableStateOf(Observation("A-1", Level.HIGH, description)) }
        BitacoraTheme {
            ObservationCard(
                observation,
                onFollow = { observation = observation.copy(followed = !observation.followed) },
                onShare = {},
                onUrgent = { observation = observation.copy(urgent = !observation.urgent) },
            )
        }
    }

    private fun toggleable(state: ToggleableState) = SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, state)

    private fun role(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    // Con el gesto de encabezados, TalkBack salta de tarjeta en tarjeta.
    @Test
    fun theCodeIsAHeading() {
        card()
        rule.onNodeWithText("A-1").assert(isHeading())
    }

    // La "A" de la insignia repite lo que dice la línea, "Alta · ...": no es una parada.
    @Test
    fun theBadgeIsNotRead() {
        card()
        rule.onAllNodesWithText("A").assertCountEquals(0)
        rule.onNodeWithText("Alta · riego roto en el sector norte").assertExists()
    }

    // "Seguir, no marcado, casilla de verificación".
    @Test
    fun followSaysWhatItIsAndWhetherItIsOn() {
        card()
        rule.onNodeWithContentDescription("Seguir").assert(role(Role.Checkbox)).assert(toggleable(ToggleableState.Off))
        rule.onNodeWithContentDescription("Seguir").performClick()
        rule.onNodeWithContentDescription("Seguir").assert(toggleable(ToggleableState.On))
    }

    @Test
    fun shareSaysWhatItIs() {
        card()
        rule.onNodeWithContentDescription("Compartir").assert(role(Role.Button))
    }

    // Compose agranda el área de toque de un botón de 24 dp hasta 48, pero no le hace sitio: las de dos botones juntos
    // se pisan, y un dedo entre los dos no sabe cuál pulsa.
    @Test
    fun followAndShareDoNotOverlap() {
        card()
        val follow = rule.onNodeWithContentDescription("Seguir").fetchSemanticsNode().touchBoundsInRoot
        val share = rule.onNodeWithContentDescription("Compartir").fetchSemanticsNode().touchBoundsInRoot
        assertFalse(follow.overlaps(share), "$follow se pisa con $share")
        assertTrue(follow.width >= 48 && share.width >= 48, "$follow, $share")
    }

    // "Urgente, desactivado, interruptor": una sola parada, con su nombre y su estado. El Switch no es otra.
    @Test
    fun urgentIsOneStop() {
        card()
        rule.onNodeWithText("Urgente").assert(role(Role.Switch)).assert(toggleable(ToggleableState.Off))
        rule.onAllNodes(isToggleable()).assertCountEquals(2)
    }

    // Toda la fila es el interruptor: también se pulsa en el texto.
    @Test
    fun theWholeRowTurnsUrgentOn() {
        card()
        rule.onNodeWithText("Urgente").performClick()
        rule.onNodeWithText("Urgente").assert(toggleable(ToggleableState.On))
    }

    // Con la letra al doble, la línea no se corta: se lee entera.
    // Con los gráficos nativos: los de por defecto de Robolectric no miden el texto de verdad.
    @Test
    @Config(fontScale = 2f)
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun withBigTextTheLineIsNotCut() {
        card()
        val results = mutableListOf<TextLayoutResult>()
        val line = rule.onNodeWithText("Alta · riego roto en el sector norte").fetchSemanticsNode()
        line.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        assertFalse(results.single().hasVisualOverflow, "la línea se corta en ${results.single().lineCount} línea")
    }
}

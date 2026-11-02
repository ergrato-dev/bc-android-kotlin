package dev.bitacora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

// La pantalla de Robolectric mide 320 dp de ancho, con densidad 1. Las cuentas, en el comentario de cada test.
@RunWith(AndroidJUnit4::class)
class ObservationRowTest {
    @get:Rule
    val rule = createComposeRule()

    private val riego = Observation("A-1", Level.HIGH, "riego roto en el sector norte", "-34.6037, -58.3816")

    private fun row(observation: Observation = riego, checked: Boolean = false, events: MutableList<Any> = mutableListOf()) =
        rule.setContent {
            ObservationRow(observation, checked, { events += "open" }, { events += it }, Modifier.testTag("row"))
        }

    // Un cuadrado de 40 con la letra del nivel.
    @Test
    fun theBadgeIsA40dpSquare() {
        rule.setContent { LevelBadge(Level.MEDIUM, Modifier.testTag("badge")) }
        rule.onNodeWithTag("badge").assertWidthIsEqualTo(40.dp).assertHeightIsEqualTo(40.dp)
        rule.onNodeWithText("M").assertExists()
    }

    // El modifier de fuera va primero: el margen de 8 queda fuera del cuadrado, que sigue midiendo 40.
    @Test
    fun theCallersModifierGoesOutside() {
        rule.setContent { Box { LevelBadge(Level.LOW, Modifier.padding(8.dp).testTag("badge")) } }
        rule.onNodeWithTag("badge").assertLeftPositionInRootIsEqualTo(8.dp).assertWidthIsEqualTo(40.dp)
    }

    // Toda la pantalla de ancho, y el margen de 16 dentro de la zona que se pulsa: 16 + 48 (la casilla) + 16 de alto,
    // como poco.
    @Test
    fun theWholeRowIsTouchable() {
        val events = mutableListOf<Any>()
        row(events = events)
        rule.onNodeWithTag("row").assertLeftPositionInRootIsEqualTo(0.dp).assertWidthIsEqualTo(320.dp).assertHeightIsAtLeast(80.dp)
        rule.onNodeWithTag("row").performClick()
        assertEquals(listOf<Any>("open"), events)
    }

    // 16 de margen + 40 de la insignia + 16 de separación = 72.
    @Test
    fun theTextsStartAfterTheBadge() {
        row()
        rule.onNodeWithText("riego roto en el sector norte", useUnmergedTree = true).assertLeftPositionInRootIsEqualTo(72.dp)
        rule.onNodeWithText("A-1 · -34.6037, -58.3816", useUnmergedTree = true).assertLeftPositionInRootIsEqualTo(72.dp)
    }

    @Test
    fun withoutPlaceItSaysSo() {
        row(Observation("A-2", Level.MEDIUM, "hojas amarillas"))
        rule.onNodeWithText("A-2 · Sin ubicación", useUnmergedTree = true).assertExists()
    }

    // La casilla ocupa 48 a la derecha, antes del margen: 320 - 16 - 48 = 256. Lo que se ve son 24 en el centro:
    // 256 + 12 = 268.
    @Test
    fun theCheckboxIsAtTheEnd() {
        row()
        val box = rule.onNode(isToggleable()).getUnclippedBoundsInRoot()
        assertEquals(268.dp, box.left)
        assertEquals(24.dp, box.right - box.left)
    }

    // Una descripción más larga que la fila no empuja la casilla fuera de la pantalla.
    @Test
    fun aLongDescriptionDoesNotPushTheCheckboxOut() {
        row(riego.copy(description = "riego ".repeat(80)))
        assertEquals(268.dp, rule.onNode(isToggleable()).getUnclippedBoundsInRoot().left)
    }

    // Marcar no abre la observación: solo avisa con el valor nuevo.
    @Test
    fun checkingDoesNotOpen() {
        val events = mutableListOf<Any>()
        row(events = events)
        rule.onNode(isToggleable()).performClick()
        assertEquals(listOf<Any>(true), events)
    }

    @Test
    fun aCheckedRowShowsItChecked() {
        row(checked = true)
        rule.onNode(isToggleable()).assertIsOn()
    }
}

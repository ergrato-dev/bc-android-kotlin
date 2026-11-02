package dev.bitacora

import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
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

// Semana 22: la pantalla sola, con un estado hecho a mano. Vale para los dos sabores. La pantalla de Robolectric mide
// 320 x 470 dp, con densidad 1.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class BitacoraScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val session = List(100) { Observation("A-${it + 1}", Level.LOW, "plaga ${it + 1}") }

    private fun screen(observations: List<Observation> = session, onOpen: (Observation) -> Unit = {}) = rule.setContent {
        BitacoraScreen("Bitácora agro v1.0.0", BitacoraUiState(observations = observations), {}, {}, {}, onOpen, {})
    }

    // A la lista le queda lo que dejan el borrador y "Compartir", y cada fila mide 56 (8 + 40 de la insignia + 8): en
    // los 470 dp de alto no caben diez. Las que no se ven no se componen.
    @Test
    fun aLongSessionOnlyComposesWhatFits() {
        screen()
        rule.onNodeWithText("A-1 · Baja · plaga 1").assertExists()
        rule.onNodeWithText("A-10 · Baja · plaga 10").assertDoesNotExist()
        rule.onNodeWithText("A-100 · Baja · plaga 100").assertDoesNotExist()
    }

    // Solo se desplaza la lista: el borrador, el resumen y "Compartir" siguen en pantalla.
    @Test
    fun theDraftAndShareStayWhileTheListScrolls() {
        screen()
        rule.onNode(hasScrollAction()).performScrollToKey("A-100")
        rule.onNodeWithText("A-100 · Baja · plaga 100").assertExists()
        rule.onNodeWithText("A-1 · Baja · plaga 1").assertDoesNotExist()
        rule.onNodeWithText("Descripción").assertExists()
        rule.onNodeWithText("100 observaciones · Baja: 100 · Media: 0 · Alta: 0").assertExists()
        rule.onNodeWithText("Compartir").assertExists()
    }

    // 320 - 16 - 16 de margen = 288; menos dos huecos de 12, 264 para tres botones: 88 cada uno.
    @Test
    fun theLevelsShareTheWidth() {
        screen(emptyList())
        rule.onNodeWithText("Baja").assertLeftPositionInRootIsEqualTo(16.dp).assertWidthIsEqualTo(88.dp)
        rule.onNodeWithText("Media").assertLeftPositionInRootIsEqualTo(116.dp).assertWidthIsEqualTo(88.dp)
        rule.onNodeWithText("Alta").assertLeftPositionInRootIsEqualTo(216.dp).assertWidthIsEqualTo(88.dp)
    }

    // La insignia, la línea y, debajo, la ubicación. Sin ubicación, nada.
    @Test
    fun aRowShowsItsBadgeAndPlace() {
        screen(listOf(Observation("A-1", Level.HIGH, "riego", Position(-34.6037, -58.3816)), Observation("A-2", Level.MEDIUM, "helada")))
        rule.onNodeWithText("A-1 · Alta · riego").assertExists()
        rule.onNodeWithText("A", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("-34.6037, -58.3816", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("M", useUnmergedTree = true).assertExists()
    }

    // La fila se pulsa entera: también por la insignia.
    @Test
    fun theWholeRowOpens() {
        val riego = Observation("A-1", Level.HIGH, "riego")
        val opened = mutableListOf<Observation>()
        screen(listOf(riego)) { opened += it }
        rule.onNodeWithText("A", useUnmergedTree = true).performClick()
        assertEquals(listOf(riego), opened)
    }
}

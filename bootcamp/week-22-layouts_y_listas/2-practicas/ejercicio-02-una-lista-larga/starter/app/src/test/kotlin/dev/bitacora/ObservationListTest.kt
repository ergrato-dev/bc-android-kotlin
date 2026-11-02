package dev.bitacora

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

// La lista sola, con createComposeRule. La pantalla de Robolectric mide 320 x 470 dp; cada fila, 72 como poco.
@RunWith(AndroidJUnit4::class)
class ObservationListTest {
    @get:Rule
    val rule = createComposeRule()

    private val high = List(500) { Observation("A-${it + 1}", Level.HIGH, "observación ${it + 1}") }

    private fun list() = rule.onNode(hasScrollAction())

    // En 470 dp caben como mucho 470 / 72 = 6,5 filas, más la cabecera: la décima ya no está en la composición.
    @Test
    fun onlyWhatFitsIsComposed() {
        rule.setContent { ObservationList(high) }
        rule.onNodeWithText("observación 1").assertExists()
        rule.onNodeWithText("observación 10").assertDoesNotExist()
        rule.onNodeWithText("observación 500").assertDoesNotExist()
    }

    @Test
    fun scrollingToAKeyComposesIt() {
        rule.setContent { ObservationList(high) }
        list().performScrollToKey("A-300")
        rule.onNodeWithText("observación 300").assertExists()
        rule.onNodeWithText("observación 1").assertDoesNotExist()
    }

    // De Alta a Baja; un nivel sin observaciones no tiene cabecera. Se compara la altura de cada texto: en el árbol de
    // semántica, la cabecera fija va al final, porque se pinta encima de las filas.
    @Test
    fun theHeadersGroupByLevel() {
        rule.setContent {
            ObservationList(
                listOf(
                    Observation("A-1", Level.LOW, "pulgón"),
                    Observation("A-2", Level.HIGH, "riego roto"),
                    Observation("A-3", Level.HIGH, "helada"),
                ),
            )
        }
        val order = listOf("Alta · 2", "riego roto", "helada", "Baja · 1", "pulgón")
        assertEquals(order, order.sortedBy { rule.onNodeWithText(it, useUnmergedTree = true).getUnclippedBoundsInRoot().top })
        rule.onNodeWithText("Media", substring = true).assertDoesNotExist()
    }

    @Test
    fun theHeaderStaysOnTop() {
        rule.setContent { ObservationList(high) }
        list().performScrollToKey("A-300")
        // El texto de la cabecera, dentro de su margen de 16.
        rule.onNodeWithText("Alta · 500").assertTopPositionInRootIsEqualTo(16.dp)
    }

    // Abrir la de "riego" y añadir otra delante: sigue abierta la de "riego", no la que ocupa ahora su sitio.
    @Test
    fun anOpenRowFollowsItsObservation() {
        var observations by mutableStateOf(listOf(Observation("A-2", Level.HIGH, "riego", "-34.6, -58.3"), Observation("A-3", Level.HIGH, "helada")))
        rule.setContent { ObservationList(observations) }
        rule.onNodeWithText("riego").performClick()
        observations = listOf(Observation("A-1", Level.HIGH, "plaga")) + observations
        rule.onNodeWithText("A-2 · -34.6, -58.3").assertExists()
        rule.onNodeWithText("A-1 · Sin ubicación").assertDoesNotExist()
    }

    @Test
    fun upOnlyWhenScrolled() {
        rule.setContent { ObservationList(high) }
        rule.onNodeWithText("Arriba").assertDoesNotExist()
        list().performScrollToIndex(10)
        rule.onNodeWithText("Arriba").performClick()
        rule.onNodeWithText("observación 1").assertExists()
        rule.onNodeWithText("Arriba").assertDoesNotExist()
    }
}

// La actividad, con la sesión larga: lo que sobrevive a la muerte del proceso.
@RunWith(AndroidJUnit4::class)
class ProcessDeathTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    // El desplazamiento se aplica en la siguiente pasada de medida: sin waitForIdle, el Bundle se llevaría la posición
    // de antes.
    private fun recreate(before: () -> Unit) {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        before()
        rule.waitForIdle()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup(saved)
    }

    @Test
    fun theScrollSurvives() {
        recreate { rule.onNode(hasScrollAction()).performScrollToKey("A-300") }
        rule.onNodeWithText("observación 300").assertExists()
    }

    // La A-300 es par: tiene ubicación.
    @Test
    fun anOpenRowSurvives() {
        recreate {
            rule.onNode(hasScrollAction()).performScrollToKey("A-300")
            rule.onNodeWithText("observación 300").performClick()
        }
        rule.onNodeWithText("A-300 · -34.300, -58.300").assertExists()
    }
}

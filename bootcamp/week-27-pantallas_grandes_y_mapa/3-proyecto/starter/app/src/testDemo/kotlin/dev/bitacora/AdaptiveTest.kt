package dev.bitacora

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 27: con 840 dp de ancho o más, la sesión a la izquierda y el detalle a la derecha. En demo, la sesión empieza
// con A-1 (con posición), A-2 y A-3.
@RunWith(AndroidJUnit4::class)
class AdaptiveTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun launch(intent: Intent? = null): ComponentActivity =
        (if (intent == null) Robolectric.buildActivity(MainActivity::class.java) else Robolectric.buildActivity(MainActivity::class.java, intent))
            .setup().get()

    private fun ComponentActivity.back() {
        rule.waitForIdle()
        onBackPressedDispatcher.onBackPressed()
    }

    private fun openRow(text: String) = rule.onNodeWithText(text, substring = true).performClick()

    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun wideShowsTheSessionAndAPlaceholder() {
        launch()
        rule.onNodeWithText("Compartir").assertExists()
        rule.onNodeWithText("Elige una observación").assertExists()
    }

    // La lista a la izquierda (0 a 360) y el detalle a la derecha, con 24 dp entre los dos.
    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun wideOpensTheDetailBesideTheList() {
        launch()
        openRow("A-2 · Media")
        rule.onNodeWithText("Elige una observación").assertDoesNotExist()
        val list = rule.onNodeWithText("Compartir").getBoundsInRoot()
        val detail = rule.onNodeWithText("hojas amarillas en la parcela 3", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(list.right <= 360.dp && detail.left >= 384.dp, "$list y $detail")
    }

    // Abrir otra cambia el detalle: atrás vuelve a la sesión sola, no a la anterior.
    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun openingAnotherReplacesTheDetail() {
        val activity = launch()
        openRow("A-2 · Media")
        openRow("A-3 · Baja")
        rule.onNodeWithText("pulgón en los tomates", useUnmergedTree = true).assertExists()
        activity.back()
        rule.onNodeWithText("Elige una observación").assertExists()
    }

    // Atrás desde el detalle vuelve a la sesión sola, y la app sigue abierta.
    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun backFromTheDetailKeepsTheApp() {
        val activity = launch()
        openRow("A-2 · Media")
        activity.back()
        rule.onNodeWithText("Elige una observación").assertExists()
        assertFalse(activity.isFinishing)
    }

    // 700 dp es medium: un solo panel, como en el móvil.
    @Test
    @Config(qualifiers = "es-w700dp-h500dp")
    fun mediumShowsOnePane() {
        launch()
        rule.onNodeWithText("Elige una observación").assertDoesNotExist()
        openRow("A-2 · Media")
        rule.onNodeWithText("Compartir").assertDoesNotExist()
    }

    // Un enlace en una pantalla ancha: la sesión debajo, como en el móvil, y las dos a la vista.
    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun aLinkOpensBesideTheList() {
        launch(Intent(Intent.ACTION_VIEW, Uri.parse("bitacora://observation/A-3")))
        rule.onNodeWithText("Compartir").assertExists()
        rule.onNodeWithText("pulgón en los tomates", useUnmergedTree = true).assertExists()
    }

    // Solo A-1 tiene posición. Con dos paneles, el mapa sale a la derecha y la lista sigue.
    @Test
    @Config(qualifiers = "es-w900dp-h600dp")
    fun theMapSaysHowManyItShows() {
        launch()
        rule.onNodeWithText("Mapa").performClick()
        rule.onNodeWithText("1 de 3 en el mapa").assertExists()
        rule.onNodeWithText("Compartir").assertExists()
    }
}

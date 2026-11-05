package dev.bitacora

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature.Orientation.Companion.HORIZONTAL
import androidx.window.layout.FoldingFeature.State.Companion.HALF_OPENED
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// No los toques. La pantalla, en ventanas de varios tamaños: Robolectric las simula con los qualifiers, y la bisagra
// la pone window-testing. La pantalla se pinta desde el test: con la de MainActivity, que se pinta en onCreate, la
// bisagra de mentira no llega (teoría 03).
@RunWith(AndroidJUnit4::class)
class SessionScreenTest {
    private val publisher = WindowLayoutInfoPublisherRule()
    private val compose = createAndroidComposeRule<ComponentActivity>()

    // La bisagra de mentira, por fuera: tiene que estar puesta antes de que arranque la actividad.
    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(publisher).around(compose)

    private fun show() = compose.setContent { BitacoraTheme { SessionScreen(observations) } }

    // Un móvil (en Robolectric, 320 x 470 dp): la lista y, al elegir, solo el detalle.
    @Test
    fun aPhoneShowsTheListOrTheDetail() {
        show()
        compose.onNodeWithText("Elige una observación").assertDoesNotExist()
        compose.onNodeWithText("A-2 · Media").performClick()
        compose.onNodeWithText("hojas amarillas en la parcela 3").assertExists()
        compose.onNodeWithText("A-1 · Alta").assertDoesNotExist()
    }

    // En un panel, atrás vuelve a la lista y no cierra la actividad.
    @Test
    fun backReturnsToTheList() {
        show()
        compose.onNodeWithText("A-2 · Media").performClick()
        compose.waitForIdle()
        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.onNodeWithText("A-1 · Alta").assertExists()
        assertFalse(compose.activity.isFinishing)
    }

    // Expanded: la lista, de 0 a 360, y a la derecha el detalle, que dice qué hacer hasta que eliges.
    @Test
    @Config(qualifiers = "w900dp-h600dp")
    fun expandedShowsBoth() {
        show()
        compose.onNodeWithText("Elige una observación").assertExists()
        compose.onNodeWithText("A-3 · Baja").performClick()
        val list = compose.onNodeWithText("A-1 · Alta").getBoundsInRoot()
        val detail = compose.onNodeWithText("pulgón en los tomates").getBoundsInRoot()
        assertTrue(list.right <= 360.dp && detail.left >= 360.dp, "$list y $detail")
    }

    // Medio plegado, con la bisagra en horizontal a media altura (en 600 x 800 dp, a 400): el detalle arriba y la lista
    // abajo.
    @Test
    @Config(qualifiers = "w600dp-h800dp")
    fun tabletopPutsTheDetailAboveTheHinge() {
        show()
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(FoldingFeature(compose.activity, state = HALF_OPENED, orientation = HORIZONTAL))))
        val detail = compose.onNodeWithText("Elige una observación").getBoundsInRoot()
        val list = compose.onNodeWithText("A-1 · Alta").getBoundsInRoot()
        assertTrue(detail.bottom <= 400.dp && list.top >= 400.dp, "$detail y $list")
    }
}

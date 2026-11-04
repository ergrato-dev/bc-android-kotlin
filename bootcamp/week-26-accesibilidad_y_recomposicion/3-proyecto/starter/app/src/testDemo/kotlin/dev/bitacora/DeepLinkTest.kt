package dev.bitacora

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Semana 24: los enlaces. En demo, la sesión empieza con A-1, A-2 y A-3.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class DeepLinkTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun link(uri: String) = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

    private fun open(uri: String) = Robolectric.buildActivity(MainActivity::class.java, link(uri)).setup().get()

    // El manifiesto lo declara: Android sabe que la Bitácora abre estos enlaces.
    @Test
    fun androidSendsTheLinksToTheBitacora() {
        val app = ApplicationProvider.getApplicationContext<android.content.Context>()
        val found = app.packageManager.queryIntentActivities(link("bitacora://observation/A-2"), PackageManager.MATCH_DEFAULT_ONLY)
        assertEquals(listOf(MainActivity::class.java.name), found.map { it.activityInfo.name })
    }

    @Test
    fun aLinkOpensItsObservation() {
        open("bitacora://observation/A-2")
        rule.onNodeWithText("A-2").assertExists()
        rule.onNodeWithText("hojas amarillas en la parcela 3").assertExists()
        rule.onNodeWithText("Compartir").assertDoesNotExist()
    }

    // Atrás desde el enlace vuelve a la sesión, no cierra la app.
    @Test
    fun backFromALinkGoesToTheSession() {
        val activity = open("bitacora://observation/A-2")
        rule.waitForIdle()
        activity.onBackPressedDispatcher.onBackPressed()
        rule.onNodeWithText("3 observaciones · Baja: 1 · Media: 1 · Alta: 1").assertExists()
        assertFalse(activity.isFinishing)
    }

    // Un enlace viene de fuera: con un código que no existe, o una ruta que no es de una observación, la sesión.
    @Test
    fun aBadLinkOpensTheSession() {
        for (uri in listOf("bitacora://observation/A-99", "bitacora://observation/", "bitacora://session/A-2")) {
            val activity = Robolectric.buildActivity(MainActivity::class.java, link(uri)).setup()
            rule.onNodeWithText("3 observaciones · Baja: 1 · Media: 1 · Alta: 1").assertExists()
            activity.pause().stop().destroy()
        }
    }
}

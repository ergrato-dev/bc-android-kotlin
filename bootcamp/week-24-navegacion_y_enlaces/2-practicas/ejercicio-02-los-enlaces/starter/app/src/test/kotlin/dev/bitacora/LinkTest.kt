package dev.bitacora

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// La sesión empieza con A-1, A-2 y A-3.
@RunWith(AndroidJUnit4::class)
class LinkTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun link(uri: String) = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

    private fun exists(code: String) = code in listOf("A-1", "A-2", "A-3")

    private fun handlers(intent: Intent) = ApplicationProvider.getApplicationContext<Context>().packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).map { it.activityInfo.name }

    private fun assertOnSession() = rule.onNodeWithText("A-1 · Alta · riego roto en el sector norte").assertExists()

    // Android sabe, por el manifiesto, que la Bitácora abre estos enlaces.
    @Test
    fun androidSendsTheLinksToTheBitacora() {
        assertEquals(listOf(MainActivity::class.java.name), handlers(link("bitacora://observation/A-2")))
    }

    // Y solo estos: otro esquema no es nuestro.
    @Test
    fun otherLinksAreNotOurs() {
        assertEquals(emptyList(), handlers(link("https://bitacora.dev/observation/A-2")))
    }

    // startOf sola, sin actividad: el enlace y lo que hay debajo.
    @Test
    fun aLinkStartsWithTheSessionUnderneath() {
        assertEquals(listOf(SessionKey, ObservationKey("A-2")), startOf(link("bitacora://observation/A-2"), ::exists))
    }

    // Lo que no encaja, o no existe, empieza en la sesión.
    @Test
    fun aBadLinkStartsOnTheSession() {
        for (uri in listOf("bitacora://observation/A-99", "bitacora://observation/", "bitacora://observation/A-2/fotos", "bitacora://session/A-2")) {
            assertEquals(listOf(SessionKey), startOf(link(uri), ::exists), uri)
        }
    }

    // Ni el del lanzador, que no trae Uri, ni uno explícito, sin acción, como el de buildActivity.
    @Test
    fun anIntentWithoutALinkStartsOnTheSession() {
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val explicit = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        assertEquals(listOf(SessionKey), startOf(launcher, ::exists))
        assertEquals(listOf(SessionKey), startOf(explicit, ::exists))
    }

    @Test
    fun aLinkOpensItsObservation() {
        Robolectric.buildActivity(MainActivity::class.java, link("bitacora://observation/A-2")).setup()
        rule.onNodeWithText("A-2").assertExists()
        rule.onNodeWithText("hojas amarillas en la parcela 3").assertExists()
    }

    // Atrás desde un enlace vuelve a la sesión, no cierra la app.
    @Test
    fun backFromALinkGoesToTheSession() {
        val activity: ComponentActivity = Robolectric.buildActivity(MainActivity::class.java, link("bitacora://observation/A-2")).setup().get()
        rule.waitForIdle()
        activity.onBackPressedDispatcher.onBackPressed()
        assertOnSession()
        assertFalse(activity.isFinishing)
    }

    @Test
    fun anUnknownCodeOpensTheSession() {
        Robolectric.buildActivity(MainActivity::class.java, link("bitacora://observation/A-99")).setup()
        assertOnSession()
    }
}

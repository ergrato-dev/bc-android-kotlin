package dev.bitacora

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Robolectric no lanza la actividad que pides: la apunta. shadowOf(actividad).nextStartedActivity devuelve el último
// Intent que se le pasó a startActivity. En su móvil no hay más apps que la tuya, salvo las que instale el test.
// MATCH_DEFAULT_ONLY busca como startActivity: solo los filtros con la categoría DEFAULT.
@RunWith(AndroidJUnit4::class)
class IntentsTest {
    private val app: Application = ApplicationProvider.getApplicationContext()
    private val note = Note("A-1", "LOW", "Riego del sector 3")

    private fun main() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun started(action: MainActivity.() -> Unit): Intent {
        val activity = main()
        activity.action()
        return assertNotNull(shadowOf(activity).nextStartedActivity, "No se lanzó ningún Intent")
    }

    private fun observation(intent: Intent) = Robolectric.buildActivity(ObservationActivity::class.java, intent).setup().get()

    @Test
    fun openingAnObservationIsExplicit() {
        val intent = started { openObservation(note) }
        assertEquals(ComponentName(app, ObservationActivity::class.java), intent.component)
        assertEquals(note, IntentCompat.getParcelableExtra(intent, EXTRA_NOTE, Note::class.java))
    }

    @Test
    fun theObservationScreenShowsTheNote() {
        val intent = started { openObservation(note) }
        assertEquals("A-1: Riego del sector 3", observation(intent).shown.text.toString())
    }

    @Test
    fun sharingGoesThroughTheChooser() {
        val chooser = started { share("A-1: Riego del sector 3") }
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = assertNotNull(IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java))
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals("A-1: Riego del sector 3", send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun theMapIsAGeoUri() {
        val intent = started { openMap(-34.6037, -58.3816) }
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse("geo:-34.6037,-58.3816"), intent.data)
    }

    @Test
    fun youCanOnlyShareIfAnAppAcceptsIt() {
        assertFalse(main().canShare())
        val mail = ComponentName("com.example.mail", "com.example.mail.ComposeActivity")
        shadowOf(app.packageManager).addActivityIfNotPresent(mail)
        shadowOf(app.packageManager).addIntentFilterForActivity(mail, IntentFilter(Intent.ACTION_SEND).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addDataType("text/plain")
        })
        assertTrue(main().canShare())
    }

    @Test
    fun theDeepLinkOpensTheObservation() {
        val link = Intent(Intent.ACTION_VIEW, Uri.parse("bitacora://observation/A-7"))
        assertEquals(listOf(ObservationActivity::class.java.name), app.packageManager.queryIntentActivities(link, MATCH_DEFAULT_ONLY).map { it.activityInfo.name })
        assertEquals("A-7", observation(link).shown.text.toString())
        val otherHost = Intent(Intent.ACTION_VIEW, Uri.parse("bitacora://map/A-7"))
        assertEquals(emptyList(), app.packageManager.queryIntentActivities(otherHost, MATCH_DEFAULT_ONLY))
    }

    @Test
    fun aLinkWithoutACodeClosesTheScreen() {
        val activity = observation(Intent(Intent.ACTION_VIEW, Uri.parse("bitacora://observation")))
        assertTrue(activity.isFinishing)
    }
}

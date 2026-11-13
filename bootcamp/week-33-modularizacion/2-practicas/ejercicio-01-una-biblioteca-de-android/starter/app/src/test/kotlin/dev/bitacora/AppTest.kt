package dev.bitacora

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.help.HelpActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// La app con la biblioteca dentro: el manifiesto y los recursos, juntos.
@RunWith(AndroidJUnit4::class)
class AppTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun help(): Pair<CharSequence, CharSequence> {
        val activity = Robolectric.buildActivity(HelpActivity::class.java).setup().get()
        val text = (activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as TextView).text
        return activity.title to text
    }

    // La actividad de la biblioteca entra en el manifiesto de la app sin que la app la declare.
    @Test
    fun theHelpActivityIsInTheApp() {
        val info = context.packageManager.getActivityInfo(ComponentName(context, HelpActivity::class.java), 0)
        assertFalse(info.exported)
    }

    @Test
    fun theAppDoesNotAskToVibrate() {
        val permissions = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty()
        assertFalse("android.permission.VIBRATE" in permissions, permissions.toList().toString())
    }

    // Con el R de la biblioteca: el nombre es suyo, el valor, de la app.
    @Test
    @Config(qualifiers = "es")
    fun theAppRenamesTheHelp() {
        assertEquals("Ayuda de la Bitácora", context.getString(dev.bitacora.help.R.string.help_title))
    }

    // Y el código de la biblioteca, que no ha cambiado, lee el de la app.
    @Test
    @Config(qualifiers = "es")
    fun theHelpScreenUsesTheAppTitle() {
        assertEquals("Ayuda de la Bitácora", help().first)
    }

    @Test
    @Config(qualifiers = "es")
    fun theRestOfTheTextsAreTheLibraryOnes() {
        assertTrue(help().second.startsWith("Desliza una observación para borrarla. Hoy, 3 consejos."), help().second.toString())
    }

    @Test
    fun theTitleIsRenamedInEnglishToo() {
        assertEquals("Bitácora help", help().first)
    }
}

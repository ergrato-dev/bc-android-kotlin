package dev.bitacora.help

import android.content.ComponentName
import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// La biblioteca, sola: sus textos, su pantalla y su manifiesto. Sin la app.
@RunWith(AndroidJUnit4::class)
class HelpTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    @Config(qualifiers = "es")
    fun oneTipInSpanish() {
        assertEquals("1 consejo", context.tips(1))
    }

    @Test
    @Config(qualifiers = "es")
    fun manyTipsInSpanish() {
        assertEquals("3 consejos", context.tips(3))
    }

    @Test
    fun tipsInEnglish() {
        assertEquals(listOf("1 tip", "2 tips"), listOf(context.tips(1), context.tips(2)))
    }

    // Sin la app, el título es el de la biblioteca.
    @Test
    @Config(qualifiers = "es")
    fun theHelpHasItsOwnTitle() {
        val activity = Robolectric.buildActivity(HelpActivity::class.java).setup().get()
        assertEquals("Ayuda", activity.title)
        assertTrue("3 consejos" in (activity.window.decorView.findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0) as TextView).text)
    }

    // La actividad está en el manifiesto de la biblioteca, con su nombre completo.
    @Test
    fun theLibraryDeclaresItsActivity() {
        val info = context.packageManager.getActivityInfo(ComponentName(context, HelpActivity::class.java), 0)
        assertEquals("dev.bitacora.help.HelpActivity", info.name)
    }
}

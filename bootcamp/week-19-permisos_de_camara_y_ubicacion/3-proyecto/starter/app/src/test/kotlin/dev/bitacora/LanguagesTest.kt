package dev.bitacora

import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class LanguagesTest {
    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun MainActivity.buttons(): List<String> {
        val layout = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as LinearLayout
        return (0 until layout.childCount).map { layout.getChildAt(it) }.filterIsInstance<Button>().map { it.text.toString() }
    }

    @Test
    @Config(qualifiers = "es")
    fun theScreenSpeaksSpanish() {
        val activity = launch()
        assertEquals(listOf("Baja", "Media", "Alta", "Compartir"), activity.buttons())
        assertEquals("Descripción", activity.description.hint.toString())
    }

    @Test
    @Config(qualifiers = "es")
    fun theSummaryUsesThePlural() {
        val activity = launch()
        activity.description.setText("riego")
        activity.add(Level.LOW)
        activity.description.setText("plaga")
        activity.add(Level.LOW)
        assertEquals("2 observaciones · Baja: 2 · Media: 0 · Alta: 0", activity.summary.text.toString())
    }

    @Test
    @Config(qualifiers = "en")
    fun theScreenIsTranslatedToEnglish() {
        val activity = launch()
        activity.description.setText("irrigation")
        activity.add(Level.MEDIUM)
        assertEquals("Field log agro v1.0.0", activity.title)
        assertEquals(listOf("Low", "Medium", "High", "Share"), activity.buttons())
        assertEquals("Description", activity.description.hint.toString())
        assertEquals("1 observation · Low: 0 · Medium: 1 · High: 0", activity.summary.text.toString())
    }

    @Test
    @Config(qualifiers = "fr")
    fun aLanguageWeDoNotHaveFallsBackToSpanish() {
        assertEquals("Bitácora agro v1.0.0", launch().title)
    }
}

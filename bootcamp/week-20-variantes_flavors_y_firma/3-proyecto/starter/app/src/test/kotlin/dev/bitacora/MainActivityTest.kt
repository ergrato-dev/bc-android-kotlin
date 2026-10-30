package dev.bitacora

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

// El móvil de Robolectric está en inglés de EE. UU.: la clase fija el idioma por defecto de la app, el español.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class MainActivityTest {
    private data class Seen(val draft: String, val field: String, val observations: List<Observation>, val summary: String)

    private fun MainActivity.seen() = Seen(notes.draft, description.text.toString(), notes.observations, summary.text.toString())

    private fun MainActivity.oneObservationAndADraft() {
        description.setText("riego del sector 3")
        add(Level.HIGH)
        description.setText("plaga en la parcela 2")
    }

    private fun rotated(): Seen {
        lateinit var after: Seen
        ActivityScenario.launch(MainActivity::class.java).use {
            it.onActivity { activity -> activity.oneObservationAndADraft() }
            RuntimeEnvironment.setQualifiers("+land")
            it.onActivity { activity -> after = activity.seen() }
        }
        return after
    }

    private fun afterProcessDeath(): Seen {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        first.get().oneObservationAndADraft()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved)
        return Robolectric.buildActivity(MainActivity::class.java).setup(saved).get().seen()
    }

    private val expected = Seen(
        "plaga en la parcela 2",
        "plaga en la parcela 2",
        listOf(Observation("A-1", Level.HIGH, "riego del sector 3")),
        "1 observación · Baja: 0 · Media: 0 · Alta: 1",
    )

    @Test
    fun theTitleHasTheDomainAndTheVersion() {
        assertEquals("Bitácora agro v1.0.0", Robolectric.buildActivity(MainActivity::class.java).setup().get().title)
    }

    @Test
    fun addingNumbersTheObservationsAndClearsTheDraft() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        activity.description.setText("riego")
        activity.add(Level.LOW)
        activity.description.setText("plaga")
        activity.add(Level.HIGH)
        assertEquals(listOf("A-1", "A-2"), activity.notes.observations.map { it.code })
        assertEquals("", activity.description.text.toString())
        assertEquals("", activity.notes.draft)
    }

    @Test
    fun anEmptyDraftAddsNothing() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        activity.description.setText("   ")
        activity.add(Level.MEDIUM)
        assertEquals(emptyList(), activity.notes.observations)
        assertEquals("0 observaciones · Baja: 0 · Media: 0 · Alta: 0", activity.summary.text.toString())
    }

    @Test
    fun everythingSurvivesARotation() = assertEquals(expected, rotated())

    @Test
    fun everythingSurvivesProcessDeath() = assertEquals(expected, afterProcessDeath())
}

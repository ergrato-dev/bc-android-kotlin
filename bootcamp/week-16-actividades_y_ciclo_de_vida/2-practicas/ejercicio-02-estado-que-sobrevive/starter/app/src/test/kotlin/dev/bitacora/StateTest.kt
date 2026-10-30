package dev.bitacora

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

// No toques este archivo. Dos formas de perder una actividad:
// - girar el móvil: Android destruye la actividad y crea otra en el mismo proceso;
// - la muerte del proceso: Android mata la app en segundo plano y, al volver, crea otra actividad en un proceso nuevo,
//   solo con lo que se guardó en el Bundle. Aquí se simula con Robolectric.buildActivity: se guarda el estado de una
//   actividad y se crea otra, desde cero, con ese Bundle.
// Lo que ve el test se lee mientras la actividad está viva: una actividad destruida ya no tiene su ViewModel.
@RunWith(AndroidJUnit4::class)
class StateTest {
    private data class Seen(val count: Int, val draft: String, val cache: String?)

    private fun MainActivity.seen() = Seen(count, draft, cache)

    private fun rotated(change: (MainActivity) -> Unit): Seen {
        lateinit var after: Seen
        ActivityScenario.launch(MainActivity::class.java).use {
            it.onActivity(change)
            RuntimeEnvironment.setQualifiers("+land")
            it.onActivity { activity -> after = activity.seen() }
        }
        return after
    }

    private fun afterProcessDeath(change: (MainActivity) -> Unit): Seen {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        change(first.get())
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved)
        return Robolectric.buildActivity(MainActivity::class.java).setup(saved).get().seen()
    }

    private fun twoObservationsAndADraft(activity: MainActivity) {
        activity.addObservation()
        activity.addObservation()
        activity.draft = "riego del sector 3"
        activity.cache = "informe calculado"
    }

    @Test
    fun startsEmpty() {
        assertEquals(Seen(0, "", null), Robolectric.buildActivity(MainActivity::class.java).setup().get().seen())
    }

    @Test
    fun theCountSurvivesARotation() = assertEquals(2, rotated(::twoObservationsAndADraft).count)

    @Test
    fun theDraftSurvivesARotation() = assertEquals("riego del sector 3", rotated(::twoObservationsAndADraft).draft)

    @Test
    fun theCacheSurvivesARotation() = assertEquals("informe calculado", rotated(::twoObservationsAndADraft).cache)

    @Test
    fun theCountSurvivesProcessDeath() = assertEquals(2, afterProcessDeath(::twoObservationsAndADraft).count)

    @Test
    fun theDraftSurvivesProcessDeath() = assertEquals("riego del sector 3", afterProcessDeath(::twoObservationsAndADraft).draft)

    @Test
    fun theCacheIsRecalculatedAfterProcessDeath() = assertNull(afterProcessDeath(::twoObservationsAndADraft).cache)
}

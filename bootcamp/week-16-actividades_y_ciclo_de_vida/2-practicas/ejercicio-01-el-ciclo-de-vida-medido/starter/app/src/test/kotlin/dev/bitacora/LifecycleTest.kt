package dev.bitacora

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

// No toques este archivo. Robolectric ejecuta la actividad en la JVM; ActivityScenario la mueve entre estados.
@RunWith(AndroidJUnit4::class)
class LifecycleTest {
    private val up = listOf("onCreate", "observer.onCreate", "onStart", "observer.onStart", "onResume", "observer.onResume")

    private fun launch(): ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    private fun ActivityScenario<MainActivity>.activity(): MainActivity {
        lateinit var activity: MainActivity
        onActivity { activity = it }
        return activity
    }

    @Test
    fun launchingGoesUpToResumed() {
        launch().use { assertEquals(up, it.activity().events) }
    }

    @Test
    fun goingToTheBackgroundStops() {
        launch().use {
            it.moveToState(Lifecycle.State.CREATED)
            assertEquals(up + listOf("observer.onPause", "onPause", "observer.onStop", "onStop"), it.activity().events)
        }
    }

    @Test
    fun comingBackStartsAgain() {
        launch().use {
            it.moveToState(Lifecycle.State.CREATED)
            it.moveToState(Lifecycle.State.RESUMED)
            assertEquals(listOf("onStart", "observer.onStart", "onResume", "observer.onResume"), it.activity().events.takeLast(4))
        }
    }

    @Test
    fun rotatingCreatesANewActivity() {
        launch().use {
            val before = it.activity()
            RuntimeEnvironment.setQualifiers("+land")
            assertNotSame(before, it.activity())
            assertEquals(up, it.activity().events)
        }
    }

    @Test
    fun theOldActivitySavesItsStateAfterStopping() {
        launch().use {
            val before = it.activity()
            RuntimeEnvironment.setQualifiers("+land")
            val down = listOf("observer.onPause", "onPause", "observer.onStop", "onStop", "onSaveInstanceState", "observer.onDestroy", "onDestroy")
            assertEquals(up + down, before.events)
        }
    }

    @Test
    fun theNewActivityKnowsItWasRestored() {
        launch().use {
            assertFalse(it.activity().restored)
            RuntimeEnvironment.setQualifiers("+land")
            assertTrue(it.activity().restored)
        }
    }

    @Test
    fun closingDestroys() {
        val scenario = launch()
        val activity = scenario.activity()
        scenario.close()
        assertEquals(listOf("observer.onDestroy", "onDestroy"), activity.events.takeLast(2))
    }
}

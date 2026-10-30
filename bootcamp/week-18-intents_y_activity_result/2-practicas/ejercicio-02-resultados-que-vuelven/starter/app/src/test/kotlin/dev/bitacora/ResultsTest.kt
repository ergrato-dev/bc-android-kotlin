package dev.bitacora

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// shadowOf(actividad).nextStartedActivityForResult es el último Intent lanzado esperando un resultado, y receiveResult
// le entrega a la actividad la respuesta como si la otra pantalla hubiera terminado. ActivityScenario.launchActivityForResult
// abre una pantalla y guarda lo que devuelve al cerrarse (scenario.result).
@RunWith(AndroidJUnit4::class)
class ResultsTest {
    private val app: Application = ApplicationProvider.getApplicationContext()

    private fun main() = Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun picked(level: Level) = Intent().putExtra(EXTRA_LEVEL, level.name)

    @Test
    fun theContractOpensThePickerWithTheCurrentLevel() {
        val intent = PickLevel().createIntent(app, Level.MEDIUM)
        assertEquals(ComponentName(app, LevelPickerActivity::class.java), intent.component)
        assertEquals("MEDIUM", intent.getStringExtra(EXTRA_LEVEL))
    }

    @Test
    fun theContractOnlyAcceptsOk() {
        assertEquals(Level.HIGH, PickLevel().parseResult(Activity.RESULT_OK, picked(Level.HIGH)))
        assertNull(PickLevel().parseResult(Activity.RESULT_CANCELED, picked(Level.HIGH)))
        assertNull(PickLevel().parseResult(Activity.RESULT_OK, null))
    }

    @Test
    fun thePickerReturnsTheChosenLevel() {
        ActivityScenario.launchActivityForResult<LevelPickerActivity>(PickLevel().createIntent(app, Level.LOW)).use {
            it.onActivity { picker -> picker.choose(Level.HIGH) }
            assertEquals(Activity.RESULT_OK, it.result.resultCode)
            assertEquals("HIGH", it.result.resultData.getStringExtra(EXTRA_LEVEL))
        }
    }

    @Test
    fun goingBackReturnsNothing() {
        ActivityScenario.launchActivityForResult<LevelPickerActivity>(PickLevel().createIntent(app, Level.LOW)).use {
            it.onActivity { picker -> picker.onBackPressedDispatcher.onBackPressed() }
            assertEquals(Activity.RESULT_CANCELED, it.result.resultCode)
            assertNull(PickLevel().parseResult(it.result.resultCode, it.result.resultData))
        }
    }

    @Test
    fun theMainScreenReceivesTheLevel() {
        val activity = main().get()
        activity.changeLevel()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        assertEquals("LOW", request.intent.getStringExtra(EXTRA_LEVEL))
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, picked(Level.HIGH))
        assertEquals("Level: HIGH", activity.shown.text.toString())
        activity.changeLevel()
        val again = shadowOf(activity).nextStartedActivityForResult
        assertEquals("HIGH", again.intent.getStringExtra(EXTRA_LEVEL))
        shadowOf(activity).receiveResult(again.intent, Activity.RESULT_CANCELED, null)
        assertEquals(Level.HIGH, activity.level)
    }

    @Test
    fun thePhotoComesFromGetContent() {
        val activity = main().get()
        activity.attachPhoto()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        assertEquals(Intent.ACTION_GET_CONTENT, request.intent.action)
        assertEquals("image/*", request.intent.type)
        assertTrue(request.intent.hasCategory(Intent.CATEGORY_OPENABLE))
        val photo = Uri.parse("content://media/external/images/media/42")
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, Intent().setData(photo))
        assertEquals(photo, activity.photo)
    }

    // El sistema mata el proceso mientras el usuario elige. Al volver, una actividad nueva recibe el resultado: el
    // registro guardó en el Bundle a qué lanzador corresponde cada requestCode.
    @Test
    fun theResultSurvivesProcessDeath() {
        val first = main()
        first.get().changeLevel()
        val request = assertNotNull(shadowOf(first.get()).nextStartedActivityForResult)
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java).setup(saved).get()
        assertTrue(second.activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, picked(Level.MEDIUM)))
        assertEquals("Level: MEDIUM", second.shown.text.toString())
    }
}

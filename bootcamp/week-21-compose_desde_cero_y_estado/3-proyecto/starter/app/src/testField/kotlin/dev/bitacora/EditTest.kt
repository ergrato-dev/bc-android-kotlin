package dev.bitacora

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.IntentCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class EditTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private val riego = Observation("A-1", Level.HIGH, "riego del sector 3")

    private fun withOneObservation() = Robolectric.buildActivity(MainActivity::class.java).setup().also {
        rule.onNodeWithText("Descripción").performTextInput("riego del sector 3")
        rule.onNodeWithText("Alta").performClick()
    }

    private fun MainActivity.openFirst(): Intent {
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").performClick()
        return assertNotNull(shadowOf(this).nextStartedActivityForResult).intent
    }

    @Test
    fun eachObservationOpensItsScreen() {
        val activity = withOneObservation().get()
        val intent = activity.openFirst()
        assertEquals(ComponentName(activity, ObservationActivity::class.java), intent.component)
        assertEquals(riego, intent.observation())
    }

    @Test
    fun theEditedObservationReplacesTheOldOne() {
        val activity = withOneObservation().get()
        val intent = activity.openFirst()
        shadowOf(activity).receiveResult(intent, Activity.RESULT_OK, Intent().putObservation(riego.copy(level = Level.LOW)))
        rule.onNodeWithText("1 observación · Baja: 1 · Media: 0 · Alta: 0").assertExists()
        rule.onNodeWithText("A-1 · Baja · riego del sector 3").assertExists()
    }

    @Test
    fun goingBackChangesNothing() {
        val activity = withOneObservation().get()
        shadowOf(activity).receiveResult(activity.openFirst(), Activity.RESULT_CANCELED, null)
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").assertExists()
    }

    @Test
    fun theObservationScreenReturnsTheNewLevel() {
        val intent = EditObservation().createIntent(ApplicationProvider.getApplicationContext(), riego)
        ActivityScenario.launchActivityForResult<ObservationActivity>(intent).use {
            it.onActivity { screen ->
                assertEquals("A-1", screen.title)
                screen.choose(Level.MEDIUM)
            }
            assertEquals(Activity.RESULT_OK, it.result.resultCode)
            assertEquals(riego.copy(level = Level.MEDIUM), it.result.resultData.observation())
        }
        ActivityScenario.launchActivityForResult<ObservationActivity>(intent).use {
            it.onActivity { screen -> screen.onBackPressedDispatcher.onBackPressed() }
            assertNull(EditObservation().parseResult(it.result.resultCode, it.result.resultData))
        }
    }

    @Test
    fun withoutAnObservationTheScreenCloses() {
        val screen = Robolectric.buildActivity(ObservationActivity::class.java).setup().get()
        assertTrue(screen.isFinishing)
    }

    @Test
    fun sharingSendsTheSummaryThroughTheChooser() {
        val activity = withOneObservation().get()
        rule.onNodeWithText("Compartir").performClick()
        val chooser = assertNotNull(shadowOf(activity).nextStartedActivity)
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = assertNotNull(IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java))
        assertEquals("text/plain", send.type)
        assertEquals("1 observación · Baja: 0 · Media: 0 · Alta: 1\nA-1 · Alta · riego del sector 3", send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun theEditArrivesAfterProcessDeath() {
        val first = withOneObservation()
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").performClick()
        val request = assertNotNull(shadowOf(first.get()).nextStartedActivityForResult)
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java).setup(saved).get()
        val edited = Intent().putObservation(riego.copy(level = Level.LOW))
        assertTrue(second.activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, edited))
        rule.onNodeWithText("A-1 · Baja · riego del sector 3").assertExists()
    }
}

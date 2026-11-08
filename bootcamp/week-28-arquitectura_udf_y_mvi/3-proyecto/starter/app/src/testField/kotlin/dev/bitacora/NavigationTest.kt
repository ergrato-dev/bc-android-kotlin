package dev.bitacora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.IntentCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Semana 24: una sola actividad. Abrir una observación ya no lanza otra actividad: cambia el destino.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class NavigationTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun withOneObservation() = Robolectric.buildActivity(MainActivity::class.java).setup().also {
        rule.onNodeWithText("Descripción").performTextInput("riego del sector 3")
        rule.onNodeWithText("Alta").performClick()
    }

    // NavDisplay recoge atrás solo cuando hay algo que quitar, y lo decide al recomponer: tras un clic, primero que
    // Compose termine.
    private fun ComponentActivity.back() {
        rule.waitForIdle()
        onBackPressedDispatcher.onBackPressed()
    }

    private fun openFirst() = rule.onNodeWithText("A-1 · Alta · riego del sector 3").performClick()

    // En la observación: su código de título, su descripción y su ubicación; la lista ya no está.
    private fun assertOnTheObservation() {
        rule.onNodeWithText("A-1").assertExists()
        rule.onNodeWithText("riego del sector 3").assertExists()
        rule.onNodeWithText("Sin ubicación").assertExists()
        rule.onNodeWithText("Compartir").assertDoesNotExist()
    }

    @Test
    fun eachObservationOpensItsScreenInTheSameActivity() {
        val activity = withOneObservation().get()
        openFirst()
        assertNull(shadowOf(activity).nextStartedActivity)
        assertOnTheObservation()
    }

    @Test
    fun choosingALevelSavesItAndGoesBack() {
        withOneObservation()
        openFirst()
        rule.onNodeWithText("Baja").performClick()
        rule.onNodeWithText("1 observación · Baja: 1 · Media: 0 · Alta: 0").assertExists()
        rule.onNodeWithText("A-1 · Baja · riego del sector 3").assertExists()
    }

    @Test
    fun goingBackChangesNothing() {
        val activity = withOneObservation().get()
        openFirst()
        activity.back()
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").assertExists()
        assertFalse(activity.isFinishing)
    }

    // Con la sesión sola en el back stack, atrás es de Android: cierra la actividad.
    @Test
    fun backOnTheSessionClosesTheApp() {
        val activity = withOneObservation().get()
        activity.back()
        assertTrue(activity.isFinishing)
    }

    @Test
    fun theOpenObservationSurvivesProcessDeath() {
        val first = withOneObservation()
        openFirst()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java).setup(saved).get()
        assertOnTheObservation()
        second.back()
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").assertExists()
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
}

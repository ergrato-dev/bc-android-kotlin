package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// La actividad entera, con createEmptyComposeRule: los nodos se buscan por su texto, como los ve el usuario.
@RunWith(AndroidJUnit4::class)
class BackStackTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup()

    private fun open(code: String) = rule.onNodeWithText(code, substring = true).performClick()

    // NavDisplay recoge atrás solo cuando hay algo que quitar, y lo decide al recomponer: primero, que Compose termine.
    private fun ComponentActivity.back() {
        rule.waitForIdle()
        onBackPressedDispatcher.onBackPressed()
        rule.waitForIdle()
    }

    private fun assertOnSession() = rule.onNodeWithText("A-1 · Alta · riego roto en el sector norte").assertExists()

    private fun assertOn(code: String, description: String) {
        rule.onNodeWithText(code).assertExists()
        rule.onNodeWithText(description).assertExists()
    }

    @Test
    fun itStartsOnTheSession() {
        launch()
        assertOnSession()
    }

    // Abrir una observación cambia de destino, no de actividad.
    @Test
    fun anObservationOpensInTheSameActivity() {
        val activity = launch().get()
        open("A-2 ·")
        assertNull(shadowOf(activity).nextStartedActivity)
        assertOn("A-2", "hojas amarillas en la parcela 3")
        rule.onNodeWithText("A-1 · Alta · riego roto en el sector norte").assertDoesNotExist()
    }

    @Test
    fun choosingALevelSavesItAndGoesBack() {
        launch()
        open("A-1 ·")
        rule.onNodeWithText("Baja").performClick()
        rule.onNodeWithText("A-1 · Baja · riego roto en el sector norte").assertExists()
    }

    @Test
    fun backGoesToTheSessionAndChangesNothing() {
        val activity = launch().get()
        open("A-1 ·")
        activity.back()
        assertOnSession()
        assertFalse(activity.isFinishing)
    }

    // Con la sesión sola, atrás es de Android: cierra la actividad.
    @Test
    fun backOnTheSessionCloses() {
        val activity = launch().get()
        assertOnSession()
        activity.back()
        assertTrue(activity.isFinishing)
    }

    // "Siguiente" apila: sesión, A-1, A-2. Atrás deshace de uno en uno.
    @Test
    fun nextStacksTheObservations() {
        val activity = launch().get()
        open("A-1 ·")
        rule.onNodeWithText("Siguiente").performClick()
        assertOn("A-2", "hojas amarillas en la parcela 3")
        activity.back()
        assertOn("A-1", "riego roto en el sector norte")
        activity.back()
        assertOnSession()
    }

    // La nota de A-1 sigue ahí al volver de A-2: el destino tapado guarda su estado.
    @Test
    fun aCoveredObservationKeepsItsNote() {
        val activity = launch().get()
        open("A-1 ·")
        rule.onNodeWithText("Nota").performTextInput("revisar la bomba")
        rule.onNodeWithText("Siguiente").performClick()
        activity.back()
        rule.onNodeWithText("revisar la bomba").assertExists()
    }

    // Al quitarla del back stack, su estado se va con ella.
    @Test
    fun aClosedObservationForgetsItsNote() {
        val activity = launch().get()
        open("A-1 ·")
        rule.onNodeWithText("Nota").performTextInput("revisar la bomba")
        activity.back()
        open("A-1 ·")
        rule.onNodeWithText("revisar la bomba").assertDoesNotExist()
    }

    // El back stack, y la nota, vuelven tras la muerte del proceso.
    @Test
    fun theBackStackSurvivesProcessDeath() {
        val first = launch()
        open("A-1 ·")
        rule.onNodeWithText("Nota").performTextInput("revisar la bomba")
        rule.waitForIdle()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java).setup(saved).get()
        assertOn("A-1", "riego roto en el sector norte")
        rule.onNodeWithText("revisar la bomba").assertExists()
        second.back()
        assertOnSession()
    }
}

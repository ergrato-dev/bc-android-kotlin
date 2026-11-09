package dev.bitacora

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import dev.bitacora.data.RoomObservationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Semana 28: la sesión con un solo estado, intents, efectos y un repositorio.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "+es")
class ArchitectureTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun withOneObservation() = rule.launch().also {
        rule.onNodeWithText("Descripción").performTextInput("riego del sector 3")
        rule.onNodeWithText("Alta").performClick()
    }

    // Cerrar la app y abrirla otra vez, sin Bundle: la observación sigue, porque ya no vive en el SavedStateHandle.
    @Test
    fun theListSurvivesClosingTheApp() {
        withOneObservation().pause().stop().destroy()
        rule.launch()
        rule.onNodeWithText("A-1 · Alta · riego del sector 3").assertExists()
    }

    // Y está en la base de datos, que es lo que lee la próxima ejecución. Semana 30: antes era observations.json.
    @Test
    fun theListIsInTheDatabase() {
        withOneObservation()
        val database = AppModule.database(ApplicationProvider.getApplicationContext())
        rule.waitUntil(5_000) {
            runBlocking { RoomObservationRepository(database).observations.first() }.any { it.description == "riego del sector 3" }
        }
        database.close()
    }

    // Compartir es un efecto: se abre el selector una vez, y girar el móvil no lo abre otra.
    @Test
    fun sharingHappensOnce() {
        val controller = withOneObservation()
        rule.onNodeWithText("Compartir").performClick()
        assertNotNull(shadowOf(controller.get()).nextStartedActivity)
        controller.recreate()
        rule.waitForIdle()
        assertNull(shadowOf(controller.get()).nextStartedActivity)
    }
}

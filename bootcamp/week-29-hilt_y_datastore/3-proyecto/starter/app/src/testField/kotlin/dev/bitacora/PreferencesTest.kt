package dev.bitacora

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.io.File

// Semana 29: los filtros de la lista, con la app de verdad (AppModule y DataStore).
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "+es")
class PreferencesTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun add(description: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    @Test
    fun onlyHighHidesTheOthers() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        add("hojas amarillas", "Baja")
        add("riego roto", "Alta")
        rule.onNodeWithText("Solo altas").performClick()
        rule.onNodeWithText("A-1 · Baja · hojas amarillas").assertDoesNotExist()
        rule.onNodeWithText("A-2 · Alta · riego roto").assertExists()
    }

    @Test
    fun theFiltersAreOffAtFirst() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Solo altas").assertIsNotSelected()
        rule.onNodeWithText("Primero las recientes").assertIsNotSelected()
    }

    // Cerrar y abrir: el filtro sigue puesto, y está en el archivo de DataStore.
    @Test
    fun theFiltersSurviveClosingTheApp() {
        val first = Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Primero las recientes").performClick()
        // Como en ArchitectureTest: sin waitForIdle, el waitUntil se agota a los 5 s y el archivo no llega.
        rule.waitForIdle()
        val file = File(ApplicationProvider.getApplicationContext<BitacoraApp>().filesDir, "datastore/settings.preferences_pb")
        rule.waitUntil(5_000) { file.exists() }
        first.pause().stop().destroy()
        Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Primero las recientes").assertIsSelected()
    }
}

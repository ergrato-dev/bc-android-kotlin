package dev.bitacora

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

// No lo toques. Desplegar el móvil es un cambio de configuración: la actividad se recrea, y la elegida sigue elegida,
// ahora al lado de la lista.
@RunWith(AndroidJUnit4::class)
class UnfoldTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun unfoldingKeepsTheSelection() {
        compose.onNodeWithText("A-2 · Media").performClick()
        RuntimeEnvironment.setQualifiers("w900dp-h600dp")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("A-1 · Alta").assertExists()
        compose.onNodeWithText("hojas amarillas en la parcela 3").assertExists()
    }
}

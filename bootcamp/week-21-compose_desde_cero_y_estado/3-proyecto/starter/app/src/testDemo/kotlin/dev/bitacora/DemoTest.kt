package dev.bitacora

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

// Solo se ejecutan en demo (testDemoDebugUnitTest). Los de src/testField, solo en field.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class DemoTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun launch() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    @Test
    fun theDemoStartsWithThreeSamples() {
        launch()
        rule.onNodeWithText("3 observaciones · Baja: 1 · Media: 1 · Alta: 1").assertExists()
        rule.onNodeWithText("A-1 · Alta · riego roto en el sector norte").assertExists()
    }

    @Test
    fun theNextObservationIsTheFourth() {
        launch()
        rule.onNodeWithText("Descripción").performTextInput("riego del sector 3")
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("A-4 · Alta · riego del sector 3").assertExists()
    }

    @Test
    fun itSaysItIsTheDemo() {
        assertEquals("Bitácora demo agro v1.0.0", launch().title)
    }

    @Test
    @Config(qualifiers = "en")
    fun inEnglishToo() {
        assertEquals("Field log demo agro v1.0.0", launch().title)
    }

    @Test
    fun itIsAnotherApp() {
        assertEquals("dev.bitacora.agro.demo.debug", launch().packageName)
    }
}

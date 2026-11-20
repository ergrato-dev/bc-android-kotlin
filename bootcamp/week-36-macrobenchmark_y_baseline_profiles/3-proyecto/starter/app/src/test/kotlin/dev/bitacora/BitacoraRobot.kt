package dev.bitacora

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput

// Semana 35: lo que hace una persona con la pantalla, con sus palabras. Los tests dicen qué; el robot sabe cómo se
// encuentra cada cosa. Si cambia un texto o un tag, cambia aquí, no en cada test.
class BitacoraRobot(private val rule: ComposeTestRule) {
    fun add(description: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    fun share() {
        rule.onNodeWithText("Compartir").performClick()
    }

    // La fila puede estar fuera de la pantalla: primero se lleva la lista hasta ella.
    fun assertRow(text: String) {
        rule.onNodeWithTag("observations").performScrollToNode(hasText(text))
        rule.onNodeWithText(text).assertIsDisplayed()
    }

    // Los hijos de la lista no son las filas: cada una deja dos (el fondo de "Borrar" y su Surface). Las filas son lo
    // que se puede pulsar dentro de la lista. Solo cuenta las que están compuestas, las que caben en la pantalla.
    fun assertRows(count: Int) {
        rule.onAllNodes(hasAnyAncestor(hasTestTag("observations")) and hasClickAction()).assertCountEquals(count)
    }
}

fun ComposeTestRule.bitacora(steps: BitacoraRobot.() -> Unit) = BitacoraRobot(this).steps()

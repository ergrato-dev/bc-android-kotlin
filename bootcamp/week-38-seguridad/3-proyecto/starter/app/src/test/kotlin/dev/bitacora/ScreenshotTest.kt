package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Semana 35: capturas con Roborazzi. Las de referencia están en src/test/screenshots, en el repositorio; con
// roborazzi.test.verify=true (gradle.properties), cada ./gradlew test compara con ellas. Para cambiarlas a propósito:
// ./gradlew :app:recordRoborazziFieldDebug, y mirar el cambio en el pull request. Sin color dinámico: con él, los
// colores dependen del fondo de pantalla del móvil, no de la Bitácora.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "+es")
class ScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    private val observations = listOf(
        Observation("A-1", Level.LOW, "helada en el sector 2"),
        Observation("A-2", Level.MEDIUM, "plaga en la parcela 4"),
        Observation("A-3", Level.HIGH, "riego roto"),
    )

    private fun levels() {
        rule.setContent {
            BitacoraTheme(dynamicColor = false) {
                Surface { Column { observations.forEach { ObservationRow(it, {}) } } }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun theLevels() = levels()

    @Test
    @Config(qualifiers = "+night")
    fun theLevelsAtNight() = levels()

    @Test
    @Config(fontScale = 1.5f)
    fun theLevelsWithLargeText() = levels()

    @Test
    fun theScreen() {
        rule.setContent {
            BitacoraTheme(dynamicColor = false) {
                BitacoraScreen("Bitácora agro", BitacoraUiState(observations = observations, draft = "poda", loaded = true), {}, {}, {}, {}, {})
            }
        }
        rule.onRoot().captureRoboImage()
    }
}

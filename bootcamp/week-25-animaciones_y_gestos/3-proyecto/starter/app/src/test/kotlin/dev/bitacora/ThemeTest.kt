package dev.bitacora

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.util.TypedValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Semana 23: el tema. GraphicsMode NATIVE hace que Robolectric pinte de verdad: captureToImage da los píxeles. Vale
// para los dos sabores. El móvil de Robolectric tiene Android 16 (API 36): hay color dinámico.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "es")
class ThemeTest {
    @get:Rule
    val rule = createComposeRule()

    private fun primaryOf(theme: @Composable (@Composable () -> Unit) -> Unit): Color {
        var primary = Color.Unspecified
        rule.setContent { theme { primary = MaterialTheme.colorScheme.primary } }
        rule.waitForIdle()
        return primary
    }

    @Test
    fun withoutDynamicColorItIsTheBrand() {
        assertEquals(LightColors.primary, primaryOf { BitacoraTheme(darkTheme = false, dynamicColor = false, content = it) })
    }

    @Test
    fun theDarkBrand() {
        assertEquals(DarkColors.primary, primaryOf { BitacoraTheme(darkTheme = true, dynamicColor = false, content = it) })
    }

    @Test
    @Config(qualifiers = "es-night")
    fun itFollowsTheSystem() {
        assertEquals(DarkColors.primary, primaryOf { BitacoraTheme(dynamicColor = false, content = it) })
    }

    // Desde Android 12 (API 31), por defecto, los colores salen del fondo de pantalla.
    @Test
    fun onAndroid12AndUpTheColorsAreDynamic() {
        val dynamic = dynamicLightColorScheme(ApplicationProvider.getApplicationContext()).primary
        val primary = primaryOf { BitacoraTheme(darkTheme = false, content = it) }
        assertEquals(dynamic, primary)
        assertNotEquals(LightColors.primary, primary)
    }

    // De noche, la pantalla entera pinta el fondo oscuro: el texto de encima no sale negro sobre negro.
    @Test
    @Config(qualifiers = "es-night")
    fun theScreenIsDarkAtNight() {
        rule.setContent {
            BitacoraTheme(dynamicColor = false) {
                BitacoraScreen("Bitácora", BitacoraUiState(), {}, {}, {}, {}, {})
            }
        }
        val pixels = rule.onRoot().captureToImage().toPixelMap()
        // En medio de la lista vacía.
        assertEquals(DarkColors.surface, pixels[160, 350])
    }

    // El color del nivel sale de los recursos, y values-night tiene el suyo: no es el de día.
    @Test
    @Config(qualifiers = "es-night")
    fun theBadgeHasItsNightColor() {
        rule.setContent { BitacoraTheme(dynamicColor = false) { LevelBadge(Level.HIGH, Modifier.testTag("badge")) } }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val night = Color(context.getColor(R.color.level_high))
        val day = Configuration(context.resources.configuration).apply { uiMode = UI_MODE_NIGHT_NO or (uiMode and UI_MODE_NIGHT_MASK.inv()) }
        assertNotEquals(Color(context.createConfigurationContext(day).getColor(R.color.level_high)), night)
        // En el borde izquierdo, a media altura: fuera de las esquinas redondeadas y de la letra.
        assertEquals(night, rule.onNodeWithTag("badge").captureToImage().toPixelMap()[2, 20])
    }

    // La barra de acción de la plataforma enseñaba el título otra vez, encima de la pantalla.
    @Test
    fun thereIsNoActionBar() {
        assertNull(Robolectric.buildActivity(MainActivity::class.java).setup().get().actionBar)
    }

    // El fondo de la ventana, el que se ve antes de que Compose pinte, también es oscuro de noche.
    @Test
    @Config(qualifiers = "es-night")
    fun theWindowIsDarkAtNight() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val value = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, value, true)
        assertTrue(Color(activity.getColor(value.resourceId)).luminance() < 0.5f)
    }
}

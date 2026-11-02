package dev.bitacora

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.util.TypedValue
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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

// Con GraphicsMode NATIVE, Robolectric pinta de verdad y captureToImage da los píxeles. El móvil de Robolectric tiene
// Android 16 (API 36): hay color dinámico.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemeTest {
    @get:Rule
    val rule = createComposeRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    // El primary que ve lo que va dentro del tema.
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
    @Config(qualifiers = "night")
    fun itFollowsTheSystem() {
        assertEquals(DarkColors.primary, primaryOf { BitacoraTheme(dynamicColor = false, content = it) })
    }

    @Test
    fun onAndroid12AndUpTheColorsAreDynamic() {
        val primary = primaryOf { BitacoraTheme(darkTheme = false, content = it) }
        assertEquals(dynamicLightColorScheme(context).primary, primary)
        assertNotEquals(LightColors.primary, primary)
    }

    // Un botón de Material pinta su fondo con primary: en el borde izquierdo, a media altura (40 dp de alto).
    @Test
    fun aButtonPaintsThePrimary() {
        rule.setContent { BitacoraTheme(dynamicColor = false) { Button({}, Modifier.testTag("b")) { Text("Añadir") } } }
        assertEquals(LightColors.primary, rule.onNodeWithTag("b").captureToImage().toPixelMap()[5, 20])
    }

    // De noche, el fondo de la pantalla es el surface oscuro. Abajo del todo, debajo del botón.
    @Test
    @Config(qualifiers = "night")
    fun theScreenIsDarkAtNight() {
        rule.setContent { BitacoraTheme(dynamicColor = false) { SessionScreen(samples, {}) } }
        assertEquals(DarkColors.surface, rule.onRoot().captureToImage().toPixelMap()[160, 460])
    }

    // values-night tiene su color: no es el de día, y es el que pinta la insignia.
    @Test
    @Config(qualifiers = "night")
    fun theBadgeHasItsNightColor() {
        rule.setContent { BitacoraTheme(dynamicColor = false) { LevelBadge(Level.HIGH, Modifier.testTag("badge")) } }
        val night = Color(context.getColor(R.color.level_high))
        val day = Configuration(context.resources.configuration).apply { uiMode = UI_MODE_NIGHT_NO or (uiMode and UI_MODE_NIGHT_MASK.inv()) }
        assertNotEquals(Color(context.createConfigurationContext(day).getColor(R.color.level_high)), night)
        assertEquals(night, rule.onNodeWithTag("badge").captureToImage().toPixelMap()[2, 20])
    }

    @Test
    fun thereIsNoActionBar() {
        assertNull(Robolectric.buildActivity(MainActivity::class.java).setup().get().actionBar)
    }

    // El fondo de la ventana, el que se ve antes de que Compose pinte.
    @Test
    @Config(qualifiers = "night")
    fun theWindowIsDarkAtNight() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val value = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, value, true)
        assertTrue(Color(activity.getColor(value.resourceId)).luminance() < 0.5f)
    }
}

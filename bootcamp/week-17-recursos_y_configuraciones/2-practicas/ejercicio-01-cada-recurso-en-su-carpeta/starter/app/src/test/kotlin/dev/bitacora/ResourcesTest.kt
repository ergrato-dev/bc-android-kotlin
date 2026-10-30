package dev.bitacora

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

// Cada test fija la configuración del "móvil" (idioma, región, modo oscuro, orientación, tamaño) y pregunta qué recurso
// le da Android. @Config(qualifiers = ...) la fija para todo el test; RuntimeEnvironment.setQualifiers, a mitad.
@RunWith(AndroidJUnit4::class)
class ResourcesTest {
    private val resources: Resources
        get() = ApplicationProvider.getApplicationContext<Context>().resources

    private fun observations(count: Int) = resources.getQuantityString(R.plurals.observations, count, count)

    private fun syncHintIn(qualifiers: String): String {
        RuntimeEnvironment.setQualifiers(qualifiers)
        return resources.getString(R.string.sync_hint)
    }

    @Test
    fun theDefaultIsEnglish() {
        assertEquals("Field log", resources.getString(R.string.app_name))
        assertEquals("0 observations", observations(0))
        assertEquals("1 observation", observations(1))
        RuntimeEnvironment.setQualifiers("fr")
        assertEquals("Field log", resources.getString(R.string.app_name))
    }

    @Test
    @Config(qualifiers = "es")
    fun spanishTranslatesTheApp() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { assertEquals("Bitácora", it.title) }
        }
        assertEquals("Sincroniza cuando el móvil tenga cobertura", resources.getString(R.string.sync_hint))
    }

    @Test
    fun latinAmericaSaysCelular() {
        assertEquals("Sincroniza cuando el móvil tenga cobertura", syncHintIn("es-rES"))
        assertEquals("Sincroniza cuando el celular tenga cobertura", syncHintIn("es-rMX"))
        assertEquals("Sincroniza cuando el celular tenga cobertura", syncHintIn("es-rAR"))
        assertEquals("Bitácora", resources.getString(R.string.app_name))
    }

    @Test
    @Config(qualifiers = "es")
    fun spanishHasItsOwnPlurals() {
        assertEquals("0 observaciones", observations(0))
        assertEquals("1 observación", observations(1))
        assertEquals("2 observaciones", observations(2))
        assertEquals("1000000 de observaciones", observations(1_000_000))
    }

    @Test
    fun nightHasADarkBackground() {
        assertEquals(0xFFFFFFFF.toInt(), resources.getColor(R.color.background, null))
        RuntimeEnvironment.setQualifiers("night")
        assertEquals(0xFF121212.toInt(), resources.getColor(R.color.background, null))
    }

    @Test
    fun landscapeHasTwoColumns() {
        assertEquals(1, resources.getInteger(R.integer.columns))
        RuntimeEnvironment.setQualifiers("+land")
        assertEquals(2, resources.getInteger(R.integer.columns))
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun tabletsHaveThreeColumnsEvenInLandscape() {
        assertEquals(3, resources.getInteger(R.integer.columns))
        RuntimeEnvironment.setQualifiers("+land")
        assertEquals(3, resources.getInteger(R.integer.columns))
    }
}

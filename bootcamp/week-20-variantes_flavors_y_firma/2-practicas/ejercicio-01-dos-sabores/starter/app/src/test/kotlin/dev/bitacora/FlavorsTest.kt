package dev.bitacora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Ya está hecho: los tests de src/test se ejecutan una vez por variante (testDemoDebugUnitTest y
// testFieldDebugUnitTest). Cada uno mira en cuál está con BuildConfig.FLAVOR, que se lee por reflexión: sin flavors,
// BuildConfig no lo tiene, y el test no compilaría.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es")
class FlavorsTest {
    private val flavor = runCatching { BuildConfig::class.java.getField("FLAVOR").get(null) }.getOrNull()
    private val demo = flavor == "demo"

    @Before
    fun theAppHasBothFlavors() {
        assertTrue("La app no tiene los flavors demo y field (FLAVOR = $flavor)", flavor in setOf("demo", "field"))
    }

    private fun app() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    @Test
    fun eachFlavorIsItsOwnApp() {
        assertEquals(if (demo) "dev.bitacora.agro.demo.debug" else "dev.bitacora.agro.debug", app().packageName)
    }

    @Test
    fun theVersionSaysWhichOneItIs() {
        assertEquals(if (demo) "1.0.0-demo-debug" else "1.0.0-debug", BuildConfig.VERSION_NAME)
    }

    @Test
    fun theDemoHasItsOwnName() {
        assertEquals(if (demo) "Bitácora demo" else "Bitácora", app().title)
    }

    @Test
    @Config(qualifiers = "en")
    fun inEnglishToo() {
        assertEquals(if (demo) "Field log demo" else "Field log", app().title)
    }

    @Test
    fun onlyTheDemoStartsWithSamples() {
        assertEquals(if (demo) 3 else 0, app().list.childCount)
    }

    @Test
    fun eachOneTalksToItsServer() {
        assertEquals(if (demo) "https://demo.bitacora.example" else "https://api.bitacora.example", app().server.text)
    }
}

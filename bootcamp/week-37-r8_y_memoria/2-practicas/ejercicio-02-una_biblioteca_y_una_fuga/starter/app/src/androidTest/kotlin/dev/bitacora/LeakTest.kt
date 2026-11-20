package dev.bitacora

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

// En un dispositivo: ./gradlew connectedDebugAndroidTest. Abre la actividad, la cierra y, si el test pasa, LeakCanary
// busca lo que debería haberse liberado y sigue en memoria.
@RunWith(AndroidJUnit4::class)
class LeakTest {
    // TODO (paso 6): la regla de LeakCanary que busca fugas cuando el test pasa.

    @Test
    fun theActivityIsReleased() {
        ActivityScenario.launch(MainActivity::class.java).close()
    }
}

package dev.bitacora.alertas

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Ya está hecho. No lo cambies.
@RunWith(RobolectricTestRunner::class)
class AlertsTest {
    private val context = RuntimeEnvironment.getApplication()

    // El Intent dice a qué actividad va: si no, abre la que quiera recibirlo, y puede ser de otra app.
    @Test
    fun theNotificationOpensItsDetail() {
        val intent = shadowOf(openIntent(context, "A-2")).savedIntent
        assertEquals(DetailActivity::class.java.name, intent.component?.className)
        assertEquals("A-2", intent.getStringExtra(EXTRA_CODE))
    }

    // Quien recibe el PendingIntent (el sistema, la app de notificaciones) no puede cambiar el Intent de dentro.
    @Test
    fun nobodyCanChangeIt() {
        assertTrue(shadowOf(openIntent(context, "A-2")).isImmutable)
    }
}

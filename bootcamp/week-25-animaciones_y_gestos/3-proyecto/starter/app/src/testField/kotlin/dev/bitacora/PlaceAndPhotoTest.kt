package dev.bitacora

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSION_GRANT_RESULTS
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class PlaceAndPhotoTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private val app: Application = ApplicationProvider.getApplicationContext()
    private val riego = Observation("A-1", Level.HIGH, "riego del sector 3", Position(-34.6037, -58.3816))

    // FileProvider guarda en una variable estática las carpetas de la primera vez, y Robolectric da a cada test una
    // carpeta de datos distinta: sin vaciarla, a partir del segundo test no encuentra la carpeta de las fotos.
    @Before
    fun forgetTheFolders() {
        (FileProvider::class.java.getDeclaredField("sCache").apply { isAccessible = true }.get(null) as MutableMap<*, *>).clear()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun fix(provider: String = LocationManager.GPS_PROVIDER) {
        shadowOf(app.getSystemService(LocationManager::class.java)).simulateLocation(Location(provider).apply {
            latitude = -34.6037
            longitude = -58.3816
        })
        idle()
    }

    private fun main() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun add(description: String, level: String) {
        rule.onNodeWithText("Descripción").performTextInput(description)
        rule.onNodeWithText(level).performClick()
    }

    // Abre la observación de esa línea: su pantalla enseña lo que se guardó.
    private fun open(line: String) = rule.onNodeWithText(line).performClick()

    private fun MainActivity.answer(granted: Boolean) {
        val request = assertNotNull(shadowOf(this).lastRequestedPermission)
        assertEquals(listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), request.requestedPermissions.toList())
        if (granted) shadowOf(app).grantPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        val result = if (granted) 0 else -1
        val data = Intent().putExtra(EXTRA_PERMISSIONS, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
            .putExtra(EXTRA_PERMISSION_GRANT_RESULTS, intArrayOf(result, result))
        activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, data)
        idle()
    }

    // La Bitácora con la observación abierta.
    private fun screen(saved: Bundle? = null) = Robolectric.buildActivity(MainActivity::class.java)
        .let { if (saved == null) it.setup() else it.setup(saved) }
        .also {
            if (saved == null) {
                add("riego del sector 3", "Alta")
                open("A-1 · Alta · riego del sector 3")
            }
        }

    private fun MainActivity.photoRequest() = assertNotNull(shadowOf(this).nextStartedActivityForResult)

    @Test
    fun withPermissionTheObservationGetsItsPlace() {
        shadowOf(app).grantPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        val activity = main()
        rule.onNodeWithText("Sin ubicación").assertExists()
        rule.onNodeWithText("Ubicar").performClick()
        idle()
        assertNull(shadowOf(activity).lastRequestedPermission)
        rule.onNodeWithText("Ubicando…").assertExists()
        fix()
        rule.onNodeWithText("-34.6037, -58.3816").assertExists()
        add("riego del sector 3", "Alta")
        rule.onNodeWithText("Sin ubicación").assertExists()
        open("A-1 · Alta · riego del sector 3")
        rule.onNodeWithText("A-1").assertExists()
        rule.onNodeWithText("-34.6037, -58.3816").assertExists()
    }

    @Test
    fun withoutPermissionItAsksAndThenLocates() {
        val activity = main()
        rule.onNodeWithText("Ubicar").performClick()
        activity.answer(granted = true)
        fix()
        rule.onNodeWithText("-34.6037, -58.3816").assertExists()
    }

    @Test
    fun deniedTheObservationHasNoPlace() {
        val activity = main()
        rule.onNodeWithText("Ubicar").performClick()
        activity.answer(granted = false)
        rule.onNodeWithText("Sin permiso de ubicación").assertExists()
        add("plaga", "Baja")
        open("A-1 · Baja · plaga")
        rule.onNodeWithText("A-1").assertExists()
        rule.onNodeWithText("Sin ubicación").assertExists()
    }

    @Test
    fun thePhotoGoesToTheCameraAppThroughTheFileProvider() {
        val activity = screen().get()
        rule.onNodeWithText("Foto").performClick()
        val request = activity.photoRequest().intent
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, request.action)
        assertEquals(
            Uri.parse("content://dev.bitacora.agro.debug.files/photos/A-1.jpg"),
            IntentCompat.getParcelableExtra(request, MediaStore.EXTRA_OUTPUT, Uri::class.java),
        )
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION, request.flags)
    }

    // La foto se guarda con el nivel: al volver a abrir la observación, la tiene.
    @Test
    fun aSavedPhotoIsKeptWithTheObservation() {
        val activity = screen().get()
        rule.onNodeWithText("Sin foto").assertExists()
        rule.onNodeWithText("Foto").performClick()
        shadowOf(activity).receiveResult(activity.photoRequest().intent, Activity.RESULT_OK, null)
        rule.onNodeWithText("Con foto").assertExists()
        rule.onNodeWithText("Baja").performClick()
        open("A-1 · Baja · riego del sector 3")
        rule.onNodeWithText("Con foto").assertExists()
    }

    @Test
    fun aCancelledPhotoChangesNothing() {
        val activity = screen().get()
        rule.onNodeWithText("Foto").performClick()
        shadowOf(activity).receiveResult(activity.photoRequest().intent, Activity.RESULT_CANCELED, null)
        rule.onNodeWithText("Sin foto").assertExists()
    }

    // Una foto hecha y volver atrás sin elegir nivel: no se guarda.
    @Test
    fun goingBackForgetsThePhoto() {
        val activity = screen().get()
        rule.onNodeWithText("Foto").performClick()
        shadowOf(activity).receiveResult(activity.photoRequest().intent, Activity.RESULT_OK, null)
        rule.waitForIdle()
        activity.onBackPressedDispatcher.onBackPressed()
        open("A-1 · Alta · riego del sector 3")
        rule.onNodeWithText("Sin foto").assertExists()
    }

    // La cámara suele hacer que Android mate la Bitácora: la foto llega a una actividad nueva, en la misma observación.
    @Test
    fun thePhotoSurvivesProcessDeath() {
        val first = screen()
        rule.onNodeWithText("Foto").performClick()
        val request = first.get().photoRequest()
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = screen(saved = saved).get()
        assertTrue(second.activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, null))
        rule.waitForIdle()
        rule.onNodeWithText("A-1").assertExists()
        rule.onNodeWithText("Con foto").assertExists()
    }
}

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

    // La observación guardada, como la recibe su pantalla al abrirla.
    private fun MainActivity.opened(line: String): Observation? {
        rule.onNodeWithText(line).performClick()
        return assertNotNull(shadowOf(this).nextStartedActivityForResult).intent.observation()
    }

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

    private fun screen(saved: Bundle? = null) = Robolectric.buildActivity(ObservationActivity::class.java, EditObservation().createIntent(app, riego))
        .let { if (saved == null) it.setup() else it.setup(saved) }

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
        assertEquals(riego, activity.opened("A-1 · Alta · riego del sector 3"))
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
        assertNull(activity.opened("A-1 · Baja · plaga")?.position)
    }

    @Test
    fun thePhotoGoesToTheCameraAppThroughTheFileProvider() {
        val activity = screen().get()
        activity.takePhoto()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult).intent
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, request.action)
        assertEquals(
            Uri.parse("content://dev.bitacora.agro.debug.files/photos/A-1.jpg"),
            IntentCompat.getParcelableExtra(request, MediaStore.EXTRA_OUTPUT, Uri::class.java),
        )
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION, request.flags)
    }

    @Test
    fun aSavedPhotoIsReturnedWithTheObservation() {
        val activity = screen().get()
        assertEquals("Sin foto", activity.photoState.text.toString())
        activity.takePhoto()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, null)
        assertEquals("Con foto", activity.photoState.text.toString())
        activity.choose(Level.LOW)
        assertEquals(riego.copy(level = Level.LOW, photo = "A-1.jpg"), shadowOf(activity).resultIntent.observation())
    }

    @Test
    fun aCancelledPhotoChangesNothing() {
        val activity = screen().get()
        activity.takePhoto()
        shadowOf(activity).receiveResult(assertNotNull(shadowOf(activity).nextStartedActivityForResult).intent, Activity.RESULT_CANCELED, null)
        assertEquals("Sin foto", activity.photoState.text.toString())
        assertNull(activity.observation.photo)
    }

    // La cámara suele hacer que Android mate la Bitácora: la foto llega a una pantalla nueva.
    @Test
    fun thePhotoSurvivesProcessDeath() {
        val first = screen()
        first.get().takePhoto()
        val request = assertNotNull(shadowOf(first.get()).nextStartedActivityForResult)
        val saved = Bundle()
        first.pause().stop().saveInstanceState(saved).destroy()
        val second = screen(saved = saved).get()
        second.activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, null)
        assertEquals("A-1.jpg", second.observation.photo)
        assertEquals("Con foto", second.photoState.text.toString())
    }
}

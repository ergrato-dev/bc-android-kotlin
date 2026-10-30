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
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import org.junit.Before
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
        assertEquals("Sin ubicación", activity.place.text.toString())
        activity.onLocate()
        idle()
        assertNull(shadowOf(activity).lastRequestedPermission)
        assertEquals("Ubicando…", activity.place.text.toString())
        fix()
        assertEquals("-34.6037, -58.3816", activity.place.text.toString())
        activity.description.setText("riego del sector 3")
        activity.add(Level.HIGH)
        assertEquals(listOf(riego), activity.notes.observations)
        assertEquals("Sin ubicación", activity.place.text.toString())
    }

    @Test
    fun withoutPermissionItAsksAndThenLocates() {
        val activity = main()
        activity.onLocate()
        activity.answer(granted = true)
        fix()
        assertEquals(Position(-34.6037, -58.3816), activity.notes.position)
    }

    @Test
    fun deniedTheObservationHasNoPlace() {
        val activity = main()
        activity.onLocate()
        activity.answer(granted = false)
        assertEquals("Sin permiso de ubicación", activity.place.text.toString())
        activity.description.setText("plaga")
        activity.add(Level.LOW)
        assertNull(activity.notes.observations.single().position)
    }

    @Test
    fun thePhotoGoesToTheCameraAppThroughTheFileProvider() {
        val activity = screen().get()
        activity.takePhoto()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult).intent
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, request.action)
        assertEquals(
            Uri.parse("content://dev.bitacora.agro.files/photos/A-1.jpg"),
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

package dev.bitacora

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSION_GRANT_RESULTS
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// En Robolectric no hay diálogo de permisos: el test hace de usuario. grantPermissions concede antes de empezar;
// lastRequestedPermission es lo que pidió la app, y answer() responde como si el usuario hubiera pulsado. Los
// resultados y la ubicación llegan por el hilo principal: idle() deja que se entreguen.
@RunWith(AndroidJUnit4::class)
class PermissionsTest {
    private val app: Application = ApplicationProvider.getApplicationContext()
    private val locations = app.getSystemService(LocationManager::class.java)

    private fun main() = Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun MainActivity.status() = status.text.toString()

    private fun MainActivity.locate(): MainActivity {
        onLocate()
        idle()
        return this
    }

    private fun MainActivity.answer(coarse: Boolean, fine: Boolean) {
        val request = assertNotNull(shadowOf(this).lastRequestedPermission, "La app no pidió ningún permiso")
        assertEquals(listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), request.requestedPermissions.toList())
        if (coarse) shadowOf(app).grantPermissions(ACCESS_COARSE_LOCATION)
        if (fine) shadowOf(app).grantPermissions(ACCESS_FINE_LOCATION)
        val results = intArrayOf(if (coarse) PERMISSION_GRANTED else PERMISSION_DENIED, if (fine) PERMISSION_GRANTED else PERMISSION_DENIED)
        val data = Intent().putExtra(EXTRA_PERMISSIONS, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)).putExtra(EXTRA_PERMISSION_GRANT_RESULTS, results)
        activityResultRegistry.dispatchResult(request.requestCode, Activity.RESULT_OK, data)
        idle()
    }

    private fun fix(provider: String) {
        shadowOf(locations).simulateLocation(Location(provider).apply {
            latitude = -34.6037
            longitude = -58.3816
        })
        idle()
    }

    private fun userNeedsAReason() = shadowOf(app.packageManager).setShouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION, true)

    @Test
    fun withPermissionItLocatesWithoutAsking() {
        shadowOf(app).grantPermissions(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        val activity = main().locate()
        assertNull(shadowOf(activity).lastRequestedPermission)
        assertEquals("Locating…", activity.status())
        fix(LocationManager.GPS_PROVIDER)
        assertEquals("-34.6037, -58.3816", activity.status())
    }

    @Test
    fun withoutPermissionItAsksForBoth() {
        val activity = main().locate()
        assertEquals(listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), shadowOf(activity).lastRequestedPermission?.requestedPermissions?.toList())
        assertEquals("No location yet", activity.status())
    }

    @Test
    fun afterAllowingItLocates() {
        val activity = main().locate()
        activity.answer(coarse = true, fine = true)
        fix(LocationManager.GPS_PROVIDER)
        assertEquals("-34.6037, -58.3816", activity.status())
    }

    @Test
    fun approximateOnlyUsesTheNetwork() {
        // En el móvil de Robolectric, el proveedor de red empieza apagado.
        shadowOf(locations).setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        val activity = main().locate()
        activity.answer(coarse = true, fine = false)
        fix(LocationManager.NETWORK_PROVIDER)
        assertEquals("About -34.6037, -58.3816", activity.status())
    }

    @Test
    fun whenTheUserNeedsAReasonItExplainsFirst() {
        userNeedsAReason()
        val activity = main().locate()
        assertNull(shadowOf(activity).lastRequestedPermission)
        assertEquals("The log saves where each observation was made. Tap Locate again to allow it.", activity.status())
        activity.locate()
        assertEquals(listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), shadowOf(activity).lastRequestedPermission?.requestedPermissions?.toList())
    }

    @Test
    fun aFirstDenialLetsYouTryAgain() {
        val activity = main().locate()
        userNeedsAReason()
        activity.answer(coarse = false, fine = false)
        assertEquals("Without location, observations have no place. Tap Locate to try again.", activity.status())
        assertEquals(View.GONE, activity.settings.visibility)
    }

    @Test
    fun deniedForeverOffersTheSettings() {
        val activity = main().locate()
        activity.answer(coarse = false, fine = false)
        assertEquals("Location is off for this app. Turn it on in the settings.", activity.status())
        assertEquals(View.VISIBLE, activity.settings.visibility)
        activity.settings.performClick()
        val intent = assertNotNull(shadowOf(activity).nextStartedActivity)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals(Uri.parse("package:dev.bitacora.agro"), intent.data)
    }
}

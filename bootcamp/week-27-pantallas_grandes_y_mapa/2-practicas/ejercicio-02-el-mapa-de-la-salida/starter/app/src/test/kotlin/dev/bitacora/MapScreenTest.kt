package dev.bitacora

import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// No los toques. Robolectric no tiene Play Services: el MapView se crea, pero solo pinta un aviso, y los marcadores no
// llegan a componerse. Lo que sí se ve es lo de alrededor del mapa, y el manifiesto.
@RunWith(AndroidJUnit4::class)
class MapScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun View.all(): List<View> =
        listOf(this) + ((this as? ViewGroup)?.let { group -> (0 until group.childCount).flatMap { group.getChildAt(it).all() } } ?: emptyList())

    // A-2 no tiene ubicación: 2 de 3.
    @Test
    fun theScreenSaysHowManyAreOnTheMap() {
        compose.onNodeWithText("Mapa").assert(isHeading())
        compose.onNodeWithText("2 de 3 en el mapa").assertExists()
    }

    // Un MapView de verdad, que en Robolectric dice que falta Play Services.
    @Test
    fun theMapIsAMapViewWithoutPlayServices() {
        val views = compose.activity.window.decorView.all()
        assertTrue(views.any { it.javaClass.name == "com.google.android.gms.maps.MapView" })
        assertTrue(views.any { (it as? TextView)?.text?.contains("Google Play") == true })
    }

    // La entrada de la clave está en el manifiesto, aunque el build no tenga mapsApiKey: sin ella, Maps no arranca.
    @Test
    fun theKeyIsInTheManifest() {
        val app = compose.activity.packageManager.getApplicationInfo(compose.activity.packageName, PackageManager.GET_META_DATA)
        assertTrue(app.metaData.containsKey("com.google.android.geo.API_KEY"))
    }

    // El mapa se baja de la red: play-services-maps pide los dos primeros, y entran solos al manifiesto final. El
    // último es de androidx.core (semana 19).
    @Test
    fun theMapBringsTheNetworkPermissions() {
        val info = compose.activity.packageManager.getPackageInfo(compose.activity.packageName, PackageManager.GET_PERMISSIONS)
        assertEquals(
            listOf("android.permission.ACCESS_NETWORK_STATE", "android.permission.INTERNET", "dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"),
            info.requestedPermissions!!.sorted(),
        )
    }
}

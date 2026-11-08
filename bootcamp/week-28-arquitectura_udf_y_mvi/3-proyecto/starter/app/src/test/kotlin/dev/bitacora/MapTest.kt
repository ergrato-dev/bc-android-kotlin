package dev.bitacora

import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

// Semana 27: el mapa. Robolectric no tiene Play Services: el MapView se crea, pero solo pinta un aviso, y los
// marcadores no llegan a componerse. Lo que se puede probar aquí es lo que hay alrededor.
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "es")
class MapTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private fun View.all(): List<View> = listOf(this) + ((this as? ViewGroup)?.let { g -> (0 until g.childCount).flatMap { g.getChildAt(it).all() } } ?: emptyList())

    // La clave va en el manifiesto aunque esté vacía: sin la entrada, Maps no arranca.
    @Test
    fun theManifestHasTheMapsKey() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val info = activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
        assertTrue(info.metaData.containsKey("com.google.android.geo.API_KEY"))
    }

    // "Mapa" abre un destino con su encabezado y un MapView de verdad, que en Robolectric dice que falta Play Services: "No es posible ejecutar la aplicación Bitácora sin los Servicios de Google Play...".
    @Test
    fun theMapIsAMapView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        rule.onNodeWithText("Mapa").performClick()
        rule.waitForIdle()
        val views = activity.window.decorView.all()
        assertTrue(views.any { it.javaClass.name == "com.google.android.gms.maps.MapView" })
        assertTrue(views.any { (it as? TextView)?.text?.contains("Google Play") == true })
    }
}

package dev.bitacora

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import dev.example.sdk.Analytics

// Este ejercicio mira el APK, no la pantalla: la app solo enseña la última ubicación conocida.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Analytics.track("open")
        setContentView(TextView(this).apply { text = lastPlace()?.let { "${it.latitude}, ${it.longitude}" } ?: getString(R.string.sync_hint) })
    }

    // Paso 5: lint para el build aquí. Sin el permiso concedido, devuelve null.
    private fun lastPlace(): Location? {
        return getSystemService(LocationManager::class.java).getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }
}

package dev.bitacora

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import java.util.Properties

// Ningún texto escrito en Kotlin: todos salen de los recursos, en el idioma del móvil.
// La pantalla la pinta BitacoraScreen; la actividad se queda con lo que es de Android: permisos, ubicación, abrir la
// pantalla de la observación y compartir.
class MainActivity : ComponentActivity() {
    private val notes: ObservationDraftViewModel by viewModels()

    // Se registra al crear la actividad: si el proceso muere mientras el usuario edita, la actividad nueva recibe el
    // resultado.
    private val edit = registerForActivityResult(EditObservation()) { edited ->
        if (edited != null) notes.replace(edited)
    }

    // Con alguna de las dos concedida, se ubica; si no, la observación se guarda sin ubicación.
    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else notes.denied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        // Paso 3: la pantalla, dentro de BitacoraTheme.
        setContent {
            val state by notes.state.collectAsStateWithLifecycle()
            BitacoraScreen(
                title = title.toString(),
                state = state,
                onDraftChange = notes::onDraftChange,
                onLocate = ::onLocate,
                onAdd = { notes.add(it) },
                onOpen = edit::launch,
                onShare = { share(state.observations) },
            )
        }
    }

    private fun onLocate() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) locate()
        else requestLocation.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
    }

    // Con la precisa, el GPS; solo con la aproximada, la red.
    private fun locate() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) return
        val precise = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        val provider = if (precise) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        notes.locating()
        LocationManagerCompat.getCurrentLocation(
            getSystemService(LocationManager::class.java), provider, CancellationSignal(), ContextCompat.getMainExecutor(this),
        ) { location ->
            notes.located(location?.let { Position(it.latitude, it.longitude) })
        }
    }

    private fun share(observations: List<Observation>) {
        val text = (listOf(resources.summary(observations)) + observations.map(resources::line)).joinToString("\n")
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }
}

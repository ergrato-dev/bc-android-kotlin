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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import java.util.Properties

// Ningún texto escrito en Kotlin: todos salen de los recursos, en el idioma del móvil.
// Una sola actividad: NavDisplay pinta el último destino del back stack, la sesión o una observación. La actividad
// se queda con lo que es de Android: permisos, ubicación, el enlace con que la abren y compartir.
class MainActivity : ComponentActivity() {
    private val notes: ObservationDraftViewModel by viewModels()

    // Con alguna de las dos concedida, se ubica; si no, la observación se guarda sin ubicación.
    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else notes.denied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        val start = startOf(intent) { notes.observation(it) != null }
        setContent {
            val state by notes.state.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(*start.toTypedArray())
            BitacoraTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = entryProvider {
                        entry<SessionKey> {
                            BitacoraScreen(
                                title = title.toString(),
                                state = state,
                                onDraftChange = notes::onDraftChange,
                                onLocate = ::onLocate,
                                onAdd = { notes.add(it) },
                                onOpen = { backStack += ObservationKey(it.code) },
                                onShare = { share(state.observations) },
                                onDelete = { notes.delete(it.code) },
                                onUndo = notes::undo,
                            )
                        }
                        entry<ObservationKey> { key ->
                            // Tras la muerte del proceso, el primer fotograma puede llegar antes que las observaciones.
                            val observation = state.observations.find { it.code == key.code } ?: return@entry
                            ObservationScreen(observation, onChoose = {
                                notes.replace(it)
                                backStack.removeLastOrNull()
                            })
                        }
                    },
                )
            }
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

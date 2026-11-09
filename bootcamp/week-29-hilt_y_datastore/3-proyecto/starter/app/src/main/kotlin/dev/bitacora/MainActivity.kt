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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
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
// Paso 4: @AndroidEntryPoint, y viewModels() sin factory.
class MainActivity : ComponentActivity() {
    private val notes: BitacoraViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as BitacoraApp
                BitacoraViewModel(createSavedStateHandle(), app.repository, app.settings)
            }
        }
    }

    // Con alguna de las dos concedida, se ubica; si no, la observación se guarda sin ubicación.
    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else notes.onIntent(BitacoraIntent.LocationDenied)
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        val start = startOf(intent) { notes.observation(it) != null }
        setContent {
            val state by notes.state.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(*start.toTypedArray())
            val snackbar = remember { SnackbarHostState() }
            val resources = LocalResources.current
            // Los efectos, uno a uno y una sola vez. Al borrar, el aviso de la anterior se cierra: Undo devuelve la
            // última borrada.
            LaunchedEffect(Unit) {
                notes.effect.collect { effect ->
                    when (effect) {
                        is BitacoraEffect.ShowUndo -> {
                            snackbar.currentSnackbarData?.dismiss()
                            val message = resources.getString(R.string.deleted, effect.code)
                            launch {
                                val result = snackbar.showSnackbar(message, resources.getString(R.string.undo), duration = SnackbarDuration.Long)
                                if (result == SnackbarResult.ActionPerformed) notes.onIntent(BitacoraIntent.Undo)
                            }
                        }
                        is BitacoraEffect.Share -> share(effect.observations)
                    }
                }
            }
            // Abrir un detalle cambia el que hay: con dos paneles, atrás vuelve a la sesión, no al detalle anterior.
            val open = { key: NavKey ->
                backStack.removeAll { it != SessionKey }
                backStack += key
            }
            BitacoraTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    // Con 840 dp o más, la sesión a la izquierda y el detalle a la derecha. Con el comportamiento por
                    // defecto, atrás desde [sesión, detalle] no cambia los paneles a la vista y cierra la app.
                    sceneStrategies = listOf(rememberListDetailSceneStrategy(backNavigationBehavior = BackNavigationBehavior.PopUntilContentChange)),
                    entryProvider = entryProvider {
                        entry<SessionKey>(metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { Choose() })) {
                            BitacoraScreen(
                                title = title.toString(),
                                state = state,
                                onDraftChange = { notes.onIntent(BitacoraIntent.EditDraft(it)) },
                                onLocate = ::onLocate,
                                onAdd = { notes.onIntent(BitacoraIntent.Add(it)) },
                                onOpen = { open(ObservationKey(it.code)) },
                                onShare = { notes.onIntent(BitacoraIntent.Share) },
                                onMap = { open(MapKey) },
                                onDelete = { notes.onIntent(BitacoraIntent.Delete(it.code)) },
                                snackbar = snackbar,
                            )
                        }
                        entry<ObservationKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                            // Tras la muerte del proceso, el primer fotograma puede llegar antes que las observaciones.
                            val observation = state.observations.find { it.code == key.code } ?: return@entry
                            ObservationScreen(observation, onChoose = {
                                notes.onIntent(BitacoraIntent.Replace(it))
                                backStack.removeLastOrNull()
                            })
                        }
                        entry<MapKey>(metadata = ListDetailSceneStrategy.detailPane()) {
                            MapScreen(state.observations, onOpen = { open(ObservationKey(it.code)) })
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
        notes.onIntent(BitacoraIntent.StartLocating)
        LocationManagerCompat.getCurrentLocation(
            getSystemService(LocationManager::class.java), provider, CancellationSignal(), ContextCompat.getMainExecutor(this),
        ) { location ->
            notes.onIntent(BitacoraIntent.Located(location?.let { Position(it.latitude, it.longitude) }))
        }
    }

    private fun share(observations: List<Observation>) {
        val text = (listOf(resources.summary(observations)) + observations.map(resources::line)).joinToString("\n")
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }
}

// El hueco del detalle con dos paneles, mientras no hay ninguna abierta.
@Composable
private fun Choose() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.choose)) }
}

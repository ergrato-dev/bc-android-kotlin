package dev.bitacora

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.bitacora.core.Level
import dev.bitacora.core.MAX_DESCRIPTION
import dev.bitacora.core.Observation
import dev.bitacora.core.countByLevel
import kotlinx.coroutines.launch

// La pantalla principal, sin estado: pinta lo que recibe y avisa de lo que hace el usuario.
// Arriba, fijos, el título, el campo, la ubicación con "Ubicar" a su derecha, los niveles repartidos a partes iguales y
// el resumen. Abajo, fijo, "Compartir". En medio, la lista, con el alto que sobra y el código como clave.
@Composable
fun BitacoraScreen(
    title: String,
    state: BitacoraUiState,
    onDraftChange: (String) -> Unit,
    onLocate: () -> Unit,
    onAdd: (Level) -> Unit,
    onOpen: (Observation) -> Unit,
    onShare: () -> Unit,
    onDelete: (Observation) -> Unit = {},
    onUndo: () -> Unit = {},
) {
    val resources = LocalResources.current
    val tooLong = state.draft.length > MAX_DESCRIPTION
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Al borrar, el aviso de la anterior se cierra: undo() devuelve la última borrada.
    val delete = { observation: Observation ->
        onDelete(observation)
        snackbar.currentSnackbarData?.dismiss()
        val message = resources.getString(R.string.deleted, observation.code)
        val undo = resources.getString(R.string.undo)
        scope.launch {
            if (snackbar.showSnackbar(message, undo, duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed) onUndo()
        }
        Unit
    }
    Surface(Modifier.fillMaxSize()) {
        Box {
            Column(Modifier.padding(16.dp)) {
                // Paso 2: el título es el encabezado de la pantalla (semantics { heading() }).
                Text(title, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = onDraftChange,
                    label = { Text(stringResource(R.string.description_hint)) },
                    supportingText = if (tooLong) {
                        { Text(stringResource(R.string.counter, state.draft.length, MAX_DESCRIPTION)) }
                    } else null,
                    isError = tooLong,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(placeText(state), Modifier.weight(1f))
                    Button(onClick = onLocate) { Text(stringResource(R.string.locate)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (level in Level.entries) {
                        Button(onClick = { onAdd(level) }, Modifier.weight(1f), enabled = state.draft.isNotBlank() && !tooLong) { Text(stringResource(level.label)) }
                    }
                }
                // Paso 3: el resumen cambia al añadir o borrar: que TalkBack lo lea sin enfocarlo (liveRegion, Polite).
                Text(resources.summary(state.observations), style = MaterialTheme.typography.titleMedium)
                LazyColumn(Modifier.weight(1f)) {
                    items(state.observations, key = { it.code }) { observation ->
                        SwipeToDelete({ delete(observation) }, Modifier.animateItem()) { actions ->
                            ObservationRow(observation, { onOpen(observation) }, actions)
                        }
                    }
                }
                Button(onClick = onShare) { Text(stringResource(R.string.share)) }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
}

// Con posición, las coordenadas; sin ella, en qué está la ubicación.
@Composable
private fun placeText(state: BitacoraUiState): String {
    val position = state.position
    return if (position != null) stringResource(R.string.place, position.latitude.toString(), position.longitude.toString())
    else stringResource(
        when (state.locating) {
            Locating.IDLE -> R.string.no_place
            Locating.LOCATING -> R.string.locating
            Locating.DENIED -> R.string.location_denied
        },
    )
}

// Fuera de Compose: la actividad también los usa para compartir.
fun Resources.summary(observations: List<Observation>): String {
    val total = getQuantityString(R.plurals.observations, observations.size, observations.size)
    val levels = observations.countByLevel().entries.joinToString(" · ") { "${getString(it.key.label)}: ${it.value}" }
    return "$total · $levels"
}

fun Resources.line(observation: Observation): String =
    getString(R.string.observation_item, observation.code, getString(observation.level.label), observation.description)

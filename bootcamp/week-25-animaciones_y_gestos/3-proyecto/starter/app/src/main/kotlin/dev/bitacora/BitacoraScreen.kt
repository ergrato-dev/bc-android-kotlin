package dev.bitacora

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

// Paso 4: onDelete y onUndo. Cada fila, en un SwipeToDelete con Modifier.animateItem(). Al borrar, onDelete y un aviso
// "A-2 borrada" con "Deshacer" (SnackbarDuration.Long) en un SnackbarHost abajo, encima de todo; si se pulsa, onUndo.
// Antes, el aviso de la anterior se cierra: deshacer es de la última.
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
) {
    val resources = LocalResources.current
    val tooLong = state.draft.length > MAX_DESCRIPTION
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
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
            Text(resources.summary(state.observations), style = MaterialTheme.typography.titleMedium)
            LazyColumn(Modifier.weight(1f)) {
                items(state.observations, key = { it.code }) { observation ->
                    ObservationRow(observation, { onOpen(observation) })
                }
            }
            Button(onClick = onShare) { Text(stringResource(R.string.share)) }
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

package dev.bitacora

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.countByLevel

// La pantalla principal, sin estado: pinta lo que recibe y avisa de lo que hace el usuario.
// Paso 3: la pantalla ocupa todo y tiene un margen de 16. Arriba, fijos, el título, el campo, la ubicación con "Ubicar"
// a su derecha, los niveles repartidos a partes iguales y el resumen. Abajo, fijo, "Compartir".
// Paso 4: entre los dos, la lista: un LazyColumn que se queda con el alto que sobra, una ObservationRow por
// observación y su código como clave.
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
    Column {
        Text(title)
        TextField(value = state.draft, onValueChange = onDraftChange, label = { Text(stringResource(R.string.description_hint)) })
        Text(placeText(state))
        Button(onClick = onLocate) { Text(stringResource(R.string.locate)) }
        for (level in Level.entries) {
            Button(onClick = { onAdd(level) }) { Text(stringResource(level.label)) }
        }
        Text(resources.summary(state.observations))
        for (observation in state.observations) {
            TextButton(onClick = { onOpen(observation) }) { Text(resources.line(observation)) }
        }
        Button(onClick = onShare) { Text(stringResource(R.string.share)) }
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

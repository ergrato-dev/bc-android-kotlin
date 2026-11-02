package dev.bitacora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Ya está hecho: las dos pantallas, sin estado de la app. No saben nada de la navegación: avisan de lo que hace el
// usuario.

// La sesión: una línea por observación, que se pulsa para abrirla.
@Composable
fun SessionScreen(observations: List<Observation>, onOpen: (Observation) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            for (observation in observations) {
                TextButton(onClick = { onOpen(observation) }) {
                    Text("${observation.code} · ${stringResource(observation.level.label)} · ${observation.description}")
                }
            }
        }
    }
}

// Una observación: su código, su descripción, una nota que solo vive mientras la pantalla está en el back stack, la
// siguiente observación y un botón por nivel.
@Composable
fun ObservationScreen(
    observation: Observation,
    onChoose: (Level) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var note by rememberSaveable { mutableStateOf("") }
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(observation.code, style = MaterialTheme.typography.titleLarge)
            Text(observation.description)
            OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.note)) })
            OutlinedButton(onClick = onNext) { Text(stringResource(R.string.next)) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (level in Level.entries) {
                    Button(onClick = { onChoose(level) }, Modifier.weight(1f)) { Text(stringResource(level.label)) }
                }
            }
        }
    }
}

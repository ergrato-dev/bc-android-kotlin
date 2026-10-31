package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel

const val MAX_LENGTH = 80

// Ya está hecho: lo que pinta la pantalla. saved son las descripciones guardadas, de la primera a la última.
data class DraftUiState(val text: String = "", val level: Level? = null, val saved: List<String> = emptyList())

// Ya está hecho: el del ejercicio 01.
@Composable
fun LevelPicker(selected: Level?, onSelect: (Level) -> Unit) {
    Column {
        for (level in Level.entries) {
            Row(Modifier.selectable(selected = level == selected, onClick = { onSelect(level) }, role = Role.RadioButton)) {
                RadioButton(selected = level == selected, onClick = null)
                Text(stringResource(level.label))
            }
        }
    }
}

// Paso 3: sin estado. Lo que pintaba DraftScreen en el ejercicio 01, ahora sacado de state, y una fila (Text) por
// cada descripción guardada. Cada cambio sale por una lambda.
@Composable
fun DraftScreen(state: DraftUiState, onTextChange: (String) -> Unit, onLevelChange: (Level) -> Unit, onSave: () -> Unit) {
}

// Paso 4: el estado del ViewModel, recogido con collectAsStateWithLifecycle, y DraftScreen con sus funciones.
@Composable
fun DraftRoute(viewModel: DraftViewModel = viewModel()) {
}

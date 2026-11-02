package dev.bitacora

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Una fila: el código, el nivel y la descripción.
// Paso 1: la fila va dentro de un SwipeToDismissBox (rememberSwipeToDismissBoxState), que solo se borra deslizando
// hacia la izquierda (enableDismissFromStartToEnd = false) y llama a onDelete en onDismiss. Detrás, el texto "Borrar",
// que no lee TalkBack (clearAndSetSemantics). La fila, con fondo (MaterialTheme.colorScheme.surface): si no, se ve lo de
// detrás.
// Paso 2: TalkBack no desliza. La fila une su semántica (semantics(mergeDescendants = true)) y ofrece la acción
// "Borrar" (customActions con un CustomAccessibilityAction), que llama a onDelete.
@Composable
fun DeletableRow(observation: Observation, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        "${observation.code} · ${stringResource(observation.level.label)} · ${observation.description}",
        modifier.fillMaxWidth().padding(vertical = 12.dp),
    )
}

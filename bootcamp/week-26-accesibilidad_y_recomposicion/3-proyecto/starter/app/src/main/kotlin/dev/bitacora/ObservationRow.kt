package dev.bitacora

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.bitacora.core.Level
import dev.bitacora.core.Observation

// El color pasa al del nivel nuevo en 300 ms, y se lee al dibujar: la insignia no se recompone en cada fotograma.
// Paso 1: la letra repite el nivel, que ya dice la línea: TalkBack lee "A, A-1 · Alta...". Que no la lea
// (clearAndSetSemantics {}, al final de la cadena).
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    val color = animateColorAsState(colorResource(level.color), tween(300), label = "badge")
    Box(
        modifier.size(40.dp).clip(MaterialTheme.shapes.small).drawBehind { drawRect(color.value) },
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(level.short))
    }
}

// Solo hacia la izquierda. El estado, en un remember: con rememberSwipeToDismissBoxState, la fila devuelta con
// "Deshacer" vuelve deslizada y se borra otra vez. "Borrar" de detrás solo se ve; para TalkBack, la acción va en el
// Modifier que recibe content, que es el del nodo con el texto de la fila.
@Composable
fun SwipeToDelete(onDelete: () -> Unit, modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    val delete = stringResource(R.string.delete)
    val threshold = SwipeToDismissBoxDefaults.positionalThreshold
    val state = remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, threshold) }
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(delete, Modifier.clearAndSetSemantics {}, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = { onDelete() },
    ) {
        Surface {
            content(Modifier.semantics { customActions = listOf(CustomAccessibilityAction(delete) { onDelete(); true }) })
        }
    }
}

@Composable
fun ObservationRow(observation: Observation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LevelBadge(observation.level)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(LocalResources.current.line(observation))
            observation.position?.let {
                Text(
                    stringResource(R.string.place, it.latitude.toString(), it.longitude.toString()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

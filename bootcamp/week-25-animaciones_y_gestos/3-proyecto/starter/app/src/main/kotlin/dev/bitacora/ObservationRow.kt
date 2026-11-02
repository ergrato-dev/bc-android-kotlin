package dev.bitacora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.bitacora.core.Level
import dev.bitacora.core.Observation

// Paso 5: el color pasa al del nivel nuevo en 300 ms (animateColorAsState con tween(300)), y se pinta al dibujar
// (drawBehind, después del clip), no con background.
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    Box(
        modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(colorResource(level.color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(level.short))
    }
}

// Paso 3: SwipeToDelete(onDelete, modifier, content: @Composable (Modifier) -> Unit), aquí: un SwipeToDismissBox que solo
// se borra hacia la izquierda, con el estado en un remember (no rememberSwipeToDismissBoxState), "Borrar" detrás sin
// semántica, y la fila en un Surface. A content le pasa un Modifier con la acción "Borrar" para TalkBack, que va en la
// fila: ObservationRow ya recibe un modifier.
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

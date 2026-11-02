package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Una observación: la insignia con su nivel, el código y, si está abierta, la descripción.
// Paso 1: un toque llama a onToggle y una pulsación larga a onChangeLevel, con combinedClickable. La pulsación larga
// lleva su etiqueta (onLongClickLabel, R.string.change_level), que es lo que lee TalkBack.
@Composable
fun ObservationCard(
    observation: Observation,
    expanded: Boolean,
    onToggle: () -> Unit,
    onChangeLevel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(observation.level)
            Spacer(Modifier.width(16.dp))
            Text(observation.code)
        }
        // Paso 2: la descripción, solo si está abierta, con AnimatedVisibility: que aparezca y se vaya poco a poco.
        if (expanded) Text(observation.description)
    }
}

// La insignia: 40 dp con la letra del nivel. Los tests la buscan por su etiqueta.
// Paso 3: el color pasa al del nuevo nivel en 300 ms (animateColorAsState con tween(300)), y se pinta en la fase de
// dibujo (drawBehind), no con background: así no recompone en cada fotograma.
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).testTag("badge"), contentAlignment = Alignment.Center) {
        Text(stringResource(level.short))
    }
}

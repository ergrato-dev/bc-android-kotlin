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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.bitacora.core.Level
import dev.bitacora.core.Observation

// Paso 4: el color del nivel sale de los recursos (colorResource), con uno para el día y otro para la noche; el
// cuadrado, con las esquinas de MaterialTheme.shapes.small. La ubicación de la fila, en bodySmall y onSurfaceVariant.
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).background(level.color), contentAlignment = Alignment.Center) {
        Text(stringResource(level.short))
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
                Text(stringResource(R.string.place, it.latitude.toString(), it.longitude.toString()))
            }
        }
    }
}

private val Level.color
    get() = when (this) {
        Level.LOW -> Color(0xFFB7E1CD)
        Level.MEDIUM -> Color(0xFFFCE8B2)
        Level.HIGH -> Color(0xFFF4C7C3)
    }

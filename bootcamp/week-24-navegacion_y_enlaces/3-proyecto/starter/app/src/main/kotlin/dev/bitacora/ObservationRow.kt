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

@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    Box(
        modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(colorResource(level.color)),
        contentAlignment = Alignment.Center,
    ) {
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
                Text(
                    stringResource(R.string.place, it.latitude.toString(), it.longitude.toString()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

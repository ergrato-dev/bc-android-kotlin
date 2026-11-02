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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Ya está hecho: la fila del ejercicio 01, sin la casilla. Abierta (expanded), enseña además el código y la ubicación.
@Composable
fun ObservationRow(observation: Observation, expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LevelBadge(observation.level)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(observation.description)
            if (expanded) {
                Text(stringResource(R.string.code_and_place, observation.code, observation.place ?: stringResource(R.string.no_place)))
            }
        }
    }
}

@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).background(level.color), contentAlignment = Alignment.Center) {
        Text(stringResource(level.short))
    }
}

private val Level.color
    get() = when (this) {
        Level.LOW -> Color(0xFFB7E1CD)
        Level.MEDIUM -> Color(0xFFFCE8B2)
        Level.HIGH -> Color(0xFFF4C7C3)
    }

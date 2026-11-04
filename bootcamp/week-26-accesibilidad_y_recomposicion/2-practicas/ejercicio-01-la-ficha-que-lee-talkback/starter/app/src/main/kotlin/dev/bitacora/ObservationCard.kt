package dev.bitacora

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Una observación: la insignia, el código, dos botones dibujados (seguir y compartir), la línea con el nivel y la
// descripción, y el interruptor de urgente. Se ve bien; lo que lee TalkBack, no.
// Paso 1: el código es el encabezado de la tarjeta (semantics { heading() }).
@Composable
fun ObservationCard(
    observation: Observation,
    onFollow: () -> Unit,
    onShare: () -> Unit,
    onUrgent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(observation.level)
            Spacer(Modifier.width(16.dp))
            Text(observation.code, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            FollowButton(observation.followed, onFollow)
            ShareButton(onShare)
        }
        // Paso 4: con la letra al doble, la línea se corta: que ocupe las líneas que necesite.
        Text(
            "${stringResource(observation.level.label)} · ${observation.description}",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // Paso 3: TalkBack se para dos veces, en "Urgente" y en un interruptor sin nombre. Que la fila entera sea el
        // interruptor (toggleable con Role.Switch) y el Switch solo se vea (onCheckedChange = null).
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.urgent), Modifier.weight(1f))
            Switch(checked = observation.urgent, onCheckedChange = { onUrgent() })
        }
    }
}

// La insignia: 40 dp con la letra del nivel, que ya dice la línea de debajo.
// Paso 1: que TalkBack no la lea (clearAndSetSemantics {}).
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    val color = colorResource(level.color)
    Box(
        modifier.size(40.dp).clip(MaterialTheme.shapes.small).drawBehind { drawRect(color) },
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(level.short))
    }
}

// Seguir: un círculo, relleno si se sigue.
// Paso 2: no dice qué es ni si está marcado, y mide 24 dp. Con toggleable (Role.Checkbox), su nombre
// (contentDescription, R.string.follow) y minimumInteractiveComponentSize() antes del size.
@Composable
fun FollowButton(followed: Boolean, onFollow: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        Modifier.size(24.dp).clickable(onClick = onFollow).drawBehind {
            if (followed) drawCircle(color) else drawCircle(color, style = Stroke(2.dp.toPx()))
        },
    )
}

// Compartir: tres puntos unidos.
// Paso 2: tampoco dice qué es (contentDescription, R.string.share, y Role.Button en el clickable), y también mide 24 dp.
@Composable
fun ShareButton(onShare: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        Modifier.size(24.dp).clickable(onClick = onShare).drawBehind {
            val a = Offset(size.width * 0.75f, size.height * 0.2f)
            val b = Offset(size.width * 0.25f, size.height * 0.5f)
            val c = Offset(size.width * 0.75f, size.height * 0.8f)
            drawLine(color, a, b, 2.dp.toPx())
            drawLine(color, b, c, 2.dp.toPx())
            for (p in listOf(a, b, c)) drawCircle(color, 3.dp.toPx(), p)
        },
    )
}

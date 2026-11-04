package dev.bitacora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Lo que pinta una fila: el modelo de la pantalla, sacado de la observación.
// Paso 1: tags es una List, y para el compilador de Compose una List puede cambiar por dentro: Item es inestable, y
// una fila con un Item nuevo se recompone aunque sea igual. Márcala @Immutable: es una data class de val que nadie
// cambia, y así Compose la compara con equals.
data class Item(val code: String, val level: Level, val description: String, val tags: List<String>)

fun Observation.toItem() = Item(code, level, description, tags)

// El borrador con "Añadir" arriba, y la lista con su clave. Al bajar, "Arriba" vuelve al principio.
@Composable
fun SessionScreen(
    observations: List<Observation>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onNextLevel: (String) -> Unit,
) {
    Recompositions.count("screen")
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // Paso 2: firstVisibleItemIndex cambia con cada fila que sale por arriba, y se lee aquí, en la composición: la
    // pantalla entera se recompone con cada una. Lo que importa es si es mayor que 0: derivedStateOf, en un remember.
    val scrolled = list.firstVisibleItemIndex > 0
    val items = observations.map { it.toItem() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(draft, onDraftChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.description_hint)) })
        Button(onClick = onAdd) { Text(stringResource(R.string.add)) }
        Box(Modifier.weight(1f)) {
            LazyColumn(state = list) {
                items(items, key = { it.code }) { row ->
                    ObservationRow(row, onClick = { onNextLevel(row.code) })
                }
            }
            if (scrolled) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { list.animateScrollToItem(0) } },
                    Modifier.align(Alignment.BottomEnd),
                ) { Text(stringResource(R.string.up), Modifier.padding(horizontal = 12.dp)) }
            }
        }
    }
}

// Una fila: la insignia, la línea y las etiquetas. Un toque pasa al nivel siguiente.
@Composable
fun ObservationRow(row: Item, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Recompositions.count(row.code)
    Row(modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        LevelBadge(row.level)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(stringResource(R.string.observation_line, row.code, stringResource(row.level.label), row.description))
            Text(row.tags.joinToString(" ") { "#$it" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// La insignia, como en la semana 25: el color se pinta al dibujar.
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
    val color = colorResource(level.color)
    Box(modifier.size(40.dp).clip(MaterialTheme.shapes.small).drawBehind { drawRect(color) }, contentAlignment = Alignment.Center) {
        Text(stringResource(level.short))
    }
}

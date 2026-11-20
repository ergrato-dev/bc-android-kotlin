package dev.bitacora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Ya está hecho: 200 parcelas que llegan de otro hilo, como de una base de datos. Mientras, "Cargando…".
data class Plot(val name: String, val crop: String, val hectares: Double)

suspend fun loadPlots(): List<Plot> = withContext(Dispatchers.IO) {
    delay(300)
    val crops = listOf("Tomate", "Maíz", "Trigo", "Vid", "Olivo")
    List(200) { Plot("Parcela ${it + 1}", crops[it % crops.size], 0.5 + it % 7) }
}

@Composable
fun Plots(load: suspend () -> List<Plot> = ::loadPlots) {
    var plots by remember { mutableStateOf<List<Plot>?>(null) }
    LaunchedEffect(Unit) { plots = load() }
    val loaded = plots
    if (loaded == null) {
        Text(stringResource(R.string.loading), Modifier.padding(16.dp))
    } else {
        // El tag es para UiAutomator: con testTagsAsResourceId (MainActivity), llega como resource-id.
        LazyColumn(Modifier.fillMaxSize().testTag("plots")) {
            items(loaded, key = { it.name }) { plot ->
                ListItem(
                    headlineContent = { Text(plot.name) },
                    supportingContent = { Text("${plot.crop} · ${plot.hectares} ha") },
                )
            }
        }
    }
}

package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Ya está hecho: las parcelas, que llegan de load() en otro hilo, como de una base de datos. Mientras, "Cargando…".
// Es para el paso 6: lo que pasa cuando el trabajo no es de Compose.
@Composable
fun Plots(load: suspend () -> List<String>) {
    var plots by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(Unit) {
        plots = withContext(Dispatchers.IO) { load() }
    }
    Column {
        val loaded = plots
        if (loaded == null) Text("Cargando…") else loaded.forEach { Text(it) }
    }
}

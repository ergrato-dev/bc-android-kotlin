package dev.bitacora

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import java.io.File

// Una observación: su descripción, su ubicación, su foto y un botón por nivel. Elegir uno la guarda con ese nivel y
// la foto; volver atrás no guarda nada. La foto la hace la app de la cámara, en un archivo que le presta el
// FileProvider. La foto hecha se guarda en el estado de este destino: la cámara suele hacer que Android mate la app.
@Composable
fun ObservationScreen(observation: Observation, onChoose: (Observation) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val name = "${observation.code}.jpg"
    var photo by rememberSaveable { mutableStateOf(observation.photo) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) photo = name
    }
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Paso 2: el código es el encabezado del detalle.
            Text(observation.code, style = MaterialTheme.typography.titleLarge)
            Text(observation.description)
            val position = observation.position
            Text(
                if (position == null) stringResource(R.string.no_place)
                else stringResource(R.string.place, position.latitude.toString(), position.longitude.toString()),
            )
            Text(stringResource(if (photo == null) R.string.without_photo else R.string.with_photo))
            OutlinedButton(onClick = {
                val file = File(context.filesDir, "photos/$name").apply { parentFile?.mkdirs() }
                takePicture.launch(FileProvider.getUriForFile(context, "${context.packageName}.files", file))
            }) { Text(stringResource(R.string.photo)) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Paso 4: el botón del nivel que tiene la observación dice que está seleccionado (semantics { selected }).
                for (level in Level.entries) {
                    Button(onClick = { onChoose(observation.copy(level = level, photo = photo)) }, Modifier.weight(1f)) {
                        Text(stringResource(level.label))
                    }
                }
            }
        }
    }
}

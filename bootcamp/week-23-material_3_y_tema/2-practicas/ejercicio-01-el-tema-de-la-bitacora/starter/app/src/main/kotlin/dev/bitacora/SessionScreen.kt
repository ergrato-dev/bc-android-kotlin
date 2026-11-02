package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// Ya está hecho: el título, las observaciones de ejemplo y un botón.
// Paso 2: la pantalla entera, dentro de un Surface: pinta el fondo del tema y da a los textos el color que va encima.
@Composable
fun SessionScreen(observations: List<Observation>, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.app_name))
        for (observation in observations) ObservationRow(observation)
        Button(onClick = onAdd) { Text(stringResource(R.string.add)) }
    }
}

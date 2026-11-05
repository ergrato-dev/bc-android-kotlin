package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

// Paso 2: dónde está, si tiene las dos coordenadas; si no, null.
fun Observation.latLng(): LatLng? = null

// Paso 2: el rectángulo con todas las que tienen posición (LatLngBounds.builder() e include); sin ninguna, null.
fun List<Observation>.bounds(): LatLngBounds? = null

@Composable
fun MapScreen(observations: List<Observation>) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.map), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            // Paso 3: cuántas salen en el mapa, de cuántas (R.string.on_map, de los recursos), en el mismo orden.
            // Paso 4: el GoogleMap con el resto del alto, un Marker por cada una con posición (con key, el código de
            // título y la descripción debajo) y, en onMapLoaded, la cámara sobre bounds().
        }
    }
}

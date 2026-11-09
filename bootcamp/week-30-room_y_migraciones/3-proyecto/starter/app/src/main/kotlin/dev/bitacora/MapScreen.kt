package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.core.area

private fun Position.toLatLng() = LatLng(latitude, longitude)

// Un marcador por observación con posición; el globo abre la observación. La cámara se mueve cuando el mapa ha cargado:
// antes, CameraUpdateFactory no está inicializada.
@Composable
fun MapScreen(observations: List<Observation>, onOpen: (Observation) -> Unit, modifier: Modifier = Modifier) {
    val placed = observations.filter { it.position != null }
    val camera = rememberCameraPositionState()
    val resources = LocalResources.current
    Column(modifier.padding(16.dp)) {
        Text(stringResource(R.string.map), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.on_map, placed.size, observations.size))
        GoogleMap(
            Modifier.fillMaxWidth().weight(1f),
            cameraPositionState = camera,
            onMapLoaded = {
                observations.area()?.let { (southWest, northEast) ->
                    camera.move(
                        if (southWest == northEast) CameraUpdateFactory.newLatLngZoom(southWest.toLatLng(), 15f)
                        else CameraUpdateFactory.newLatLngBounds(LatLngBounds(southWest.toLatLng(), northEast.toLatLng()), 64),
                    )
                }
            },
        ) {
            for (observation in placed) {
                key(observation.code) {
                    Marker(
                        state = rememberUpdatedMarkerState(observation.position!!.toLatLng()),
                        title = observation.code,
                        snippet = resources.line(observation),
                        onInfoWindowClick = { onOpen(observation) },
                    )
                }
            }
        }
    }
}

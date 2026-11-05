package dev.bitacora

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.Posture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

enum class Layout { ONE_PANE, TWO_PANES, TABLETOP }

// Paso 1: medio plegado con la bisagra en horizontal (posture.isTabletop), TABLETOP, aunque la ventana sea ancha; con
// 840 dp o más (WIDTH_DP_EXPANDED_LOWER_BOUND), TWO_PANES; con menos, ONE_PANE.
fun layoutFor(sizeClass: WindowSizeClass, posture: Posture): Layout = Layout.ONE_PANE

// La sesión: la lista y el detalle de la elegida. Por ahora, siempre en un panel.
@Composable
fun SessionScreen(observations: List<Observation>) {
    // Paso 2: el tamaño y la postura de la ventana, con currentWindowAdaptiveInfoV2().
    // Paso 5: la elegida tiene que sobrevivir a la recreación de la actividad.
    var selected by remember { mutableStateOf<String?>(null) }
    val observation = observations.find { it.code == selected }
    val list = @Composable { modifier: Modifier -> ObservationList(observations, onSelect = { selected = it.code }, modifier) }
    val detail = @Composable { modifier: Modifier -> Detail(observation, modifier) }
    Surface(Modifier.fillMaxSize()) {
        // Paso 3: un when sobre layoutFor. TWO_PANES, una Row con la lista de 360 dp y el detalle con el resto;
        // TABLETOP, una Column con el detalle hasta la bisagra y la lista debajo.
        // Paso 4: en ONE_PANE, con una elegida, atrás vuelve a la lista (BackHandler).
        if (observation == null) list(Modifier.fillMaxSize()) else detail(Modifier.fillMaxSize())
    }
}

@Composable
private fun ObservationList(observations: List<Observation>, onSelect: (Observation) -> Unit, modifier: Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (observation in observations) {
            Text(
                "${observation.code} · ${stringResource(observation.level.label)}",
                Modifier.fillMaxWidth().clickable { onSelect(observation) }.padding(vertical = 12.dp),
            )
        }
    }
}

// Ya está hecho: sin elegida, qué hacer; con una, su código como encabezado y su descripción.
@Composable
private fun Detail(observation: Observation?, modifier: Modifier) {
    Box(modifier.padding(16.dp), contentAlignment = if (observation == null) Alignment.Center else Alignment.TopStart) {
        if (observation == null) Text(stringResource(R.string.choose))
        else Column {
            Text(observation.code, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            Text(observation.description)
        }
    }
}

package dev.bitacora

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Paso 1: un LazyColumn con una fila por observación, agrupadas por nivel, de Alta a Baja. La clave de cada fila es su
// código.
// Paso 2: antes de cada grupo, una cabecera fija (stickyHeader) con "Nivel · N"; los niveles sin observaciones, sin
// cabecera.
// Paso 3: cada fila guarda si está abierta en un rememberSaveable, y pulsarla la abre o la cierra.
// Paso 4: el estado de la lista en un rememberLazyListState. Cuando la primera fila visible no es la primera, un botón
// "Arriba", abajo a la derecha, vuelve al principio. Se decide con derivedStateOf.
@Composable
fun ObservationList(observations: List<Observation>, modifier: Modifier = Modifier) {
}

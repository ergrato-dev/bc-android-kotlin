package dev.bitacora

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Paso 1: un cuadrado de 40 dp con el color del nivel y su letra en el centro (stringResource(level.short)). El
// modifier de quien la llama va primero en la cadena.
@Composable
fun LevelBadge(level: Level, modifier: Modifier = Modifier) {
}

// Paso 2: una Row que ocupa todo el ancho y se pulsa entera, con un margen de 16 por dentro: la insignia, 16 de
// separación, el texto (la descripción y, debajo, "código · ubicación") y, al final, la casilla.
// Paso 3: el texto se queda con lo que sobra (weight), para que una descripción larga no saque la casilla de la fila.
@Composable
fun ObservationRow(
    observation: Observation,
    checked: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
}

package dev.bitacora

import androidx.compose.runtime.Composable

const val MAX_LENGTH = 80

// Paso 1: sin estado. Una fila seleccionable (Modifier.selectable) por nivel, con un RadioButton y su texto.
// Elegir una llama a onSelect; la marcada es la que dice selected, y nada más.
@Composable
fun LevelPicker(selected: Level?, onSelect: (Level) -> Unit) {
}

// Paso 2: con estado. El texto, el nivel y cuántas se han guardado, en rememberSaveable.
// Paso 3: el campo "Descripción", el contador "N/80", LevelPicker, el botón "Guardar" y "Guardadas: N".
@Composable
fun DraftScreen() {
}

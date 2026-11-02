package dev.bitacora

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Lo más largo que puede ser una descripción.
const val MAX_DESCRIPTION = 80

// Sin estado: pinta lo que recibe y avisa de lo que hace el usuario.
// Paso 1: una Column con 16 dp de margen. El título ("Nueva observación") con el estilo titleLarge del tema.
// Paso 2: un OutlinedTextField a todo el ancho, de una línea, con la etiqueta "Descripción" y, debajo, la cuenta
// ("5 / 80", R.string.counter). Con más de MAX_DESCRIPTION, en error. El teclado empieza las frases con mayúscula y su
// botón de acción es "Hecho" (ImeAction.Done), que guarda si se puede.
// Paso 3: los tres niveles en una SingleChoiceSegmentedButtonRow, con el elegido marcado.
// Paso 4: "Guardar", que solo se puede pulsar con una descripción que valga y un nivel elegido.
@Composable
fun ObservationForm(
    draft: String,
    level: Level?,
    onDraftChange: (String) -> Unit,
    onLevelChange: (Level) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
}

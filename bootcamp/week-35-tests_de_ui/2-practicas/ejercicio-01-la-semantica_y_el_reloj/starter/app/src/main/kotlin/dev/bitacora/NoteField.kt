package dev.bitacora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// Lo que se espera después de la última tecla para guardar.
const val SAVE_AFTER = 1_000L

// Una nota que se guarda sola: un segundo después de la última tecla, llama a onSave con el texto. Si se escribe
// antes, la espera empieza otra vez. Debajo, en qué está: nada, "Guardando…" o "Guardado".
//
// TODO paso 3: la espera y el guardado. Un LaunchedEffect con el texto como clave: cada tecla cancela el anterior y
// lanza otro. Dentro, delay(SAVE_AFTER), onSave(text) y saved = text. Con el texto vacío, no guardes.
@Composable
fun NoteField(onSave: (String) -> Unit, modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    Column(modifier) {
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), label = { Text("Nota") })
        Text(
            when (text) {
                saved -> if (text.isEmpty()) "" else "Guardado"
                else -> "Guardando…"
            },
        )
    }
}

package dev.bitacora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// Ya está hecho: el estado del formulario, guardado con rememberSaveable. Guardar enseña la observación y vacía el
// formulario.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var draft by rememberSaveable { mutableStateOf("") }
            var level by rememberSaveable { mutableStateOf<Level?>(null) }
            BitacoraTheme {
                ObservationForm(
                    draft = draft,
                    level = level,
                    onDraftChange = { draft = it },
                    onLevelChange = { level = it },
                    onSave = {
                        Toast.makeText(this, "${getString(level!!.label)} · ${draft.trim()}", Toast.LENGTH_SHORT).show()
                        draft = ""
                        level = null
                    },
                )
            }
        }
    }
}

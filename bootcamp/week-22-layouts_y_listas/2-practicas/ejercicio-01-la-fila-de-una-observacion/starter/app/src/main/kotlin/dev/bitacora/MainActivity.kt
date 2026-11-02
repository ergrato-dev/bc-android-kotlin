package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

// Ya está hecho: las tres de ejemplo, una debajo de otra. Marcar una la marca; pulsarla no hace nada todavía.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val checked = remember { mutableStateListOf<String>() }
            Column {
                for (observation in samples) {
                    ObservationRow(
                        observation = observation,
                        checked = observation.code in checked,
                        onClick = {},
                        onCheckedChange = { if (it) checked += observation.code else checked -= observation.code },
                    )
                }
            }
        }
    }
}

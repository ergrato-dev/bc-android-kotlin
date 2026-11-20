package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bitacora.sensors.Sensors

// Ya está hecho, con una fuga que encontrarás en el paso 6.
class MainActivity : ComponentActivity() {
    private val readings = mutableStateListOf<String>()

    // Una lambda que usa readings: guarda una referencia a esta actividad.
    private val listener: (String) -> Unit = { readings += it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sensors.subscribe(listener)
        Sensors.publish(Sensors.load(resources.getStringArray(R.array.probes).toList()))
        setContent {
            MaterialTheme {
                Surface {
                    Column(Modifier.padding(16.dp)) {
                        readings.forEach { Text(it) }
                    }
                }
            }
        }
    }

}

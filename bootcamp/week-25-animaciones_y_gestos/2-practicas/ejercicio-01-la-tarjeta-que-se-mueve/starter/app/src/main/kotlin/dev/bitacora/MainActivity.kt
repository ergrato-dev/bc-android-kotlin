package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Ya está hecho: una tarjeta por observación, con una sola abierta a la vez.
class MainActivity : ComponentActivity() {
    private val session: Session by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val observations by session.observations.collectAsStateWithLifecycle()
            var open by rememberSaveable { mutableStateOf<String?>(null) }
            BitacoraTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (observation in observations) {
                            ObservationCard(
                                observation,
                                expanded = open == observation.code,
                                onToggle = { open = if (open == observation.code) null else observation.code },
                                onChangeLevel = { session.nextLevel(observation.code) },
                            )
                        }
                    }
                }
            }
        }
    }
}

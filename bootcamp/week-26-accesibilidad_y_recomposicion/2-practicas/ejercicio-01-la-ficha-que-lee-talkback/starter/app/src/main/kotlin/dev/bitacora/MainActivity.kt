package dev.bitacora

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Ya está hecho: una tarjeta por observación.
class MainActivity : ComponentActivity() {
    private val session: Session by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val observations by session.observations.collectAsStateWithLifecycle()
            BitacoraTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (observation in observations) {
                            ObservationCard(
                                observation,
                                onFollow = { session.follow(observation.code) },
                                onShare = { share(observation) },
                                onUrgent = { session.urgent(observation.code) },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun share(observation: Observation) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "${observation.code} · ${observation.description}")
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }
}

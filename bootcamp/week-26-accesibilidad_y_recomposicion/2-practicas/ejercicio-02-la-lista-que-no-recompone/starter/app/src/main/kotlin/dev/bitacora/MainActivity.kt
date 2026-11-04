package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Ya está hecho: la sesión, en la pantalla.
class MainActivity : ComponentActivity() {
    private val session: Session by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val observations by session.observations.collectAsStateWithLifecycle()
            val draft by session.draft.collectAsStateWithLifecycle()
            BitacoraTheme {
                Surface {
                    SessionScreen(observations, draft, session::onDraftChange, session::add, session::nextLevel)
                }
            }
        }
    }
}

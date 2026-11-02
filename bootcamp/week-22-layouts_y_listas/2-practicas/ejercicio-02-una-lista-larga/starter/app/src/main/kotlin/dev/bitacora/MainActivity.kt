package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// Ya está hecho: la sesión larga, en la lista.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ObservationList(longSession) }
    }
}

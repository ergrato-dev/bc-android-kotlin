package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

// Ya está hecho: una sola actividad, con la sesión y la navegación dentro del tema.
class MainActivity : ComponentActivity() {
    private val session: Session by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BitacoraTheme {
                BitacoraNav(session)
            }
        }
    }
}

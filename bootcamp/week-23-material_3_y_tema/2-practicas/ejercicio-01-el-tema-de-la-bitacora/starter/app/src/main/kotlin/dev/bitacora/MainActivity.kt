package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// Ya está hecho: la pantalla, dentro del tema. "Añadir" no hace nada todavía.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BitacoraTheme {
                SessionScreen(samples, onAdd = {})
            }
        }
    }
}

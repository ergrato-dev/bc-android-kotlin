package dev.bitacora

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

// Ya está hecho: la pantalla solo lee un recurso. Este ejercicio mira el APK, no la pantalla.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = getString(R.string.sync_hint) })
    }
}

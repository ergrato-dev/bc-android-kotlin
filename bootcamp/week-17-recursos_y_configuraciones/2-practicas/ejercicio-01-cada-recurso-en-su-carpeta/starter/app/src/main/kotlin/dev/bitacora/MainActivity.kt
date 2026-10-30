package dev.bitacora

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

// Ya está hecho: la pantalla solo lee recursos. Cuál de ellos le toca lo decide Android según la configuración.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = getString(R.string.sync_hint)
            setBackgroundColor(getColor(R.color.background))
        })
    }
}

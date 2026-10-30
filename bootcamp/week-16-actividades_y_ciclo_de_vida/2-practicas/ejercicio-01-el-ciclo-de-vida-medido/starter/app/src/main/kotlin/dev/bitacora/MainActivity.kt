package dev.bitacora

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    // Ya está hecho: lo que ha pasado en esta instancia de la actividad, en orden. Los tests lo leen.
    val events = mutableListOf<String>()

    // Paso 2: true si Android creó esta instancia con un estado guardado (savedInstanceState no es null).
    var restored = false

    // Paso 1: apunta "onCreate" en events. Después, sobrescribe onStart, onResume, onPause, onStop, onSaveInstanceState
    //         y onDestroy: cada uno llama a super y apunta su nombre ("onStart"...).
    // Paso 4: registra un LifecycleLogger(events) en lifecycle.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Bitácora" })
    }
}

package dev.bitacora

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class LevelPickerActivity : ComponentActivity() {
    // Ya está hecho: un botón por nivel.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            for (level in Level.entries) {
                addView(Button(context).apply {
                    text = level.name
                    setOnClickListener { choose(level) }
                })
            }
        })
    }

    // Paso 2: devuelve RESULT_OK con el nivel en EXTRA_LEVEL y cierra la pantalla.
    fun choose(level: Level): Unit = TODO("Paso 2")
}

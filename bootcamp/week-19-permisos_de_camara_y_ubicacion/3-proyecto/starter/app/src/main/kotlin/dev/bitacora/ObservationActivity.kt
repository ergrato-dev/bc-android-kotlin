package dev.bitacora

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import dev.bitacora.core.Level
import dev.bitacora.core.Observation

// Una observación y un botón por nivel. Elegir uno la devuelve con ese nivel; volver atrás no devuelve nada.
// Paso 7: la ubicación, si la tiene; si tiene foto o no; y un botón Foto que lanza TakePicture con un archivo de
//         files/photos/ (el código y .jpg). Si la cámara la guarda, la observación lleva su nombre.
// Paso 8: esa foto se guarda en onSaveInstanceState: la cámara suele hacer que Android mate la Bitácora.
class ObservationActivity : ComponentActivity() {
    lateinit var observation: Observation
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observation = intent.observation() ?: run {
            finish()
            return
        }
        title = observation.code
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = observation.description })
            for (level in Level.entries) {
                addView(Button(context).apply {
                    setText(level.label)
                    setOnClickListener { choose(level) }
                })
            }
        })
    }

    fun choose(level: Level) {
        setResult(RESULT_OK, Intent().putObservation(observation.copy(level = level)))
        finish()
    }
}

package dev.bitacora

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

const val EXTRA_NOTE = "dev.bitacora.extra.NOTE"

class MainActivity : ComponentActivity() {
    private val note = Note("A-7", "HIGH", "Plaga en la parcela 2")

    // Ya está hecho: la pantalla solo llama a las funciones de abajo.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(Button(context).apply {
                text = note.code
                setOnClickListener { openObservation(note) }
            })
            addView(Button(context).apply {
                setText(R.string.share)
                isEnabled = canShare()
                setOnClickListener { share("${note.code}: ${note.text}") }
            })
            addView(Button(context).apply {
                setText(R.string.map)
                setOnClickListener { openMap(-34.6037, -58.3816) }
            })
        })
    }

    // Paso 2: un Intent explícito a ObservationActivity con la nota en el extra EXTRA_NOTE.
    fun openObservation(note: Note): Unit = TODO("Paso 2")

    // Paso 3: ACTION_SEND de tipo text/plain con el texto en EXTRA_TEXT, dentro de Intent.createChooser.
    fun share(text: String): Unit = TODO("Paso 3")

    // Paso 3: ACTION_VIEW con la Uri geo:<latitud>,<longitud>.
    fun openMap(latitude: Double, longitude: Double): Unit = TODO("Paso 3")

    // Paso 4: ¿hay alguna app que acepte ACTION_SEND de tipo text/plain?
    fun canShare(): Boolean = false
}

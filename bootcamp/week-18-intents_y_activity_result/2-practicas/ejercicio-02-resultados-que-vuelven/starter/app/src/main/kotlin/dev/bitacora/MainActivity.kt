package dev.bitacora

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    var level = Level.LOW
        private set
    var photo: Uri? = null
        private set
    lateinit var shown: TextView
        private set

    // Paso 3: un lanzador para PickLevel, registrado al crear la actividad (como propiedad). Si llega un nivel,
    //         guárdalo en `level` y llama a show(); si llega null, no cambia nada.
    // Paso 4: otro para ActivityResultContracts.GetContent(): la Uri que llegue va a `photo`.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shown = TextView(this)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(shown)
            addView(Button(context).apply {
                setText(R.string.change_level)
                setOnClickListener { changeLevel() }
            })
            addView(Button(context).apply {
                setText(R.string.attach_photo)
                setOnClickListener { attachPhoto() }
            })
        })
        show()
    }

    // Paso 3: lanza PickLevel con el nivel actual.
    fun changeLevel(): Unit = TODO("Paso 3")

    // Paso 4: lanza GetContent para elegir una imagen ("image/*").
    fun attachPhoto(): Unit = TODO("Paso 4")

    private fun show() {
        shown.text = getString(R.string.level, level.name)
    }
}

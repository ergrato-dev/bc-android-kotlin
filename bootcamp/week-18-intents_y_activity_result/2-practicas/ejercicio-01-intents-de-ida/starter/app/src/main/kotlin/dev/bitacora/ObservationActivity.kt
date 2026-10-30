package dev.bitacora

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class ObservationActivity : ComponentActivity() {
    lateinit var shown: TextView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shown = TextView(this)
        setContentView(shown)
        // Paso 2: lee la nota de EXTRA_NOTE con IntentCompat.getParcelableExtra y muestra "<código>: <texto>".
        // Paso 5: si no hay nota, el código sale del deep link (bitacora://observation/A-7 muestra "A-7"). Si tampoco
        //         hay código, la actividad se cierra (finish()): cualquier app puede abrirla con lo que quiera.
        TODO("Paso 2")
    }
}

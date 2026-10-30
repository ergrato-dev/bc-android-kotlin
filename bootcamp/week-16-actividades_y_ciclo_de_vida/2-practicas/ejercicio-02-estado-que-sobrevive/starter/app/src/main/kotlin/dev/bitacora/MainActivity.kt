package dev.bitacora

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

// Una pantalla de notas de campo: cuántas observaciones llevas, el borrador de la siguiente y una caché que se puede
// volver a calcular. Los tests usan solo lo público de aquí: count, addObservation(), draft y cache.
//
// Ahora todo vive en campos de la actividad y se pierde al girar el móvil.
// Paso 3: que la actividad no guarde nada. Pide el ViewModel con `by viewModels()` y que count, addObservation(),
//         draft y cache lo lean y lo escriban en él.
class MainActivity : ComponentActivity() {
    var count = 0
        private set

    var draft = ""

    var cache: String? = null

    fun addObservation() {
        count++
        draft = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Bitácora" })
    }
}

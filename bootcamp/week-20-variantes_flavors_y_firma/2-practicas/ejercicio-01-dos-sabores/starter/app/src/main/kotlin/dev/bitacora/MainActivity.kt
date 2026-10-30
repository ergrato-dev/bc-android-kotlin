package dev.bitacora

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    lateinit var server: TextView
        private set
    lateinit var list: LinearLayout
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Paso 5: el servidor de la variante, BuildConfig.SERVER_URL.
        server = TextView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // Paso 4: una fila por cada observación de Samples.observations.
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(server)
            addView(list)
        })
    }
}

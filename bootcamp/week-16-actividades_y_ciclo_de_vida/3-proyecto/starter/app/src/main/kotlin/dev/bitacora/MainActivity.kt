package dev.bitacora

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.countByLevel
import dev.bitacora.data.decode
import dev.bitacora.data.encode
import java.util.Properties

// Paso 6: ComponentActivity, con el ViewModel del paso 5. La pantalla: el título con el dominio y la versión, un campo
//         para la descripción, un botón por nivel y el resumen por nivel (README del proyecto).
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        val text = encode(listOf(Observation("A-1", Level.HIGH, "riego")))
        setContentView(TextView(this).apply {
            this.text = "Bitácora ${info.getProperty("domain")} v${info.getProperty("version")}\n$text\n${decode(text).countByLevel()}"
        })
    }
}

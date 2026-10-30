package dev.bitacora

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import dev.bitacora.core.Level
import dev.bitacora.core.countByLevel
import java.util.Properties

// Ningún texto escrito en Kotlin: todos salen de los recursos, en el idioma del móvil.
// Paso 4: un lanzador de EditObservation, registrado como propiedad; el resultado sustituye a la observación.
// Paso 5: debajo del resumen, un botón por observación (código, nivel y descripción) que la abre para editarla.
// Paso 6: un botón para compartir el resumen con otra app, a través del selector de Android.
class MainActivity : ComponentActivity() {
    val notes: ObservationDraftViewModel by viewModels()
    lateinit var summary: TextView
        private set
    lateinit var description: EditText
        private set

    // core no sabe nada de Android: el texto de cada nivel lo decide la app.
    // Paso 1: ObservationActivity también lo necesita. Llévalo a su propio archivo, como `internal`.
    private val Level.label: Int
        get() = when (this) {
            Level.LOW -> R.string.level_low
            Level.MEDIUM -> R.string.level_medium
            Level.HIGH -> R.string.level_high
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        summary = TextView(this)
        description = EditText(this).apply {
            setHint(R.string.description_hint)
            setText(notes.draft)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    notes.draft = s.toString()
                }
            })
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = title })
            addView(description)
            for (level in Level.entries) {
                addView(Button(context).apply {
                    setText(level.label)
                    setOnClickListener { add(level) }
                })
            }
            addView(summary)
        }
        setContentView(layout)
        showSummary()
    }

    fun add(level: Level) {
        if (notes.add(level) != null) description.setText("")
        showSummary()
    }

    private fun showSummary() {
        val observations = notes.observations
        val total = resources.getQuantityString(R.plurals.observations, observations.size, observations.size)
        val levels = observations.countByLevel().entries.joinToString(" · ") { "${getString(it.key.label)}: ${it.value}" }
        summary.text = "$total · $levels"
    }
}

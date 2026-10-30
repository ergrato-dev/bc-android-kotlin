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

// Paso 3: ningún texto escrito en Kotlin. El título, la pista, los botones y el resumen salen de los recursos. El
//         texto de cada nivel lo decide la app, no core (que no sabe nada de Android).
class MainActivity : ComponentActivity() {
    val notes: ObservationDraftViewModel by viewModels()
    lateinit var summary: TextView
        private set
    lateinit var description: EditText
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = "Bitácora ${info.getProperty("domain")} v${info.getProperty("version")}"
        summary = TextView(this)
        description = EditText(this).apply {
            hint = "Descripción"
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
                    text = level.name
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
        summary.text = notes.observations.countByLevel().entries.joinToString(" · ") { "${it.key}: ${it.value}" }
    }
}

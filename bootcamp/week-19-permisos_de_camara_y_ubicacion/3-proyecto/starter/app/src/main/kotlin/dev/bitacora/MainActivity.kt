package dev.bitacora

import android.content.Intent
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
import dev.bitacora.core.Observation
import dev.bitacora.core.countByLevel
import java.util.Properties

// Ningún texto escrito en Kotlin: todos salen de los recursos, en el idioma del móvil.
// Paso 5: un botón Ubicar y un texto con la ubicación del borrador. Con permiso, la busca; si no, pide los dos de
//         ubicación; denegados, lo dice. Con la precisa, el GPS; solo con la aproximada, la red.
class MainActivity : ComponentActivity() {
    val notes: ObservationDraftViewModel by viewModels()
    lateinit var summary: TextView
        private set
    lateinit var description: EditText
        private set
    lateinit var list: LinearLayout
        private set

    // Se registra al crear la actividad: si el proceso muere mientras el usuario edita, la actividad nueva recibe el
    // resultado.
    private val edit = registerForActivityResult(EditObservation()) { edited ->
        if (edited != null) {
            notes.replace(edited)
            showSummary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        summary = TextView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
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
            addView(list)
            addView(Button(context).apply {
                setText(R.string.share)
                setOnClickListener { share() }
            })
        }
        setContentView(layout)
        showSummary()
    }

    fun add(level: Level) {
        if (notes.add(level) != null) description.setText("")
        showSummary()
    }

    fun share() {
        val text = (listOf(summary.text) + notes.observations.map { it.line() }).joinToString("\n")
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }

    private fun Observation.line() = getString(R.string.observation_item, code, getString(level.label), description)

    private fun showSummary() {
        val observations = notes.observations
        val total = resources.getQuantityString(R.plurals.observations, observations.size, observations.size)
        val levels = observations.countByLevel().entries.joinToString(" · ") { "${getString(it.key.label)}: ${it.value}" }
        summary.text = "$total · $levels"
        list.removeAllViews()
        for (observation in observations) {
            list.addView(Button(this).apply {
                text = observation.line()
                setOnClickListener { edit.launch(observation) }
            })
        }
    }
}

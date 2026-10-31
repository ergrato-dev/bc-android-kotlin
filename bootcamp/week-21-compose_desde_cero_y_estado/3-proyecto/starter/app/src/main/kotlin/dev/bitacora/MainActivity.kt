package dev.bitacora

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.core.countByLevel
import java.util.Properties

// Ningún texto escrito en Kotlin: todos salen de los recursos, en el idioma del móvil.
// Paso 4: la pantalla, sin estado, en BitacoraScreen.kt.
// Paso 5: setContent con BitacoraScreen y el estado del ViewModel, recogido con collectAsStateWithLifecycle. Fuera las
// vistas, y fuera lo público que solo usaban los tests.
class MainActivity : ComponentActivity() {
    val notes: ObservationDraftViewModel by viewModels()
    lateinit var summary: TextView
        private set
    lateinit var description: EditText
        private set
    lateinit var list: LinearLayout
        private set
    lateinit var place: TextView
        private set

    // Se registra al crear la actividad: si el proceso muere mientras el usuario edita, la actividad nueva recibe el
    // resultado.
    private val edit = registerForActivityResult(EditObservation()) { edited ->
        if (edited != null) {
            notes.replace(edited)
            showSummary()
        }
    }

    // Con alguna de las dos concedida, se ubica; si no, la observación se guarda sin ubicación.
    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else place.setText(R.string.location_denied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        title = getString(R.string.title, info.getProperty("domain"), info.getProperty("version"))
        summary = TextView(this)
        place = TextView(this)
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
            addView(place)
            addView(Button(context).apply {
                setText(R.string.locate)
                setOnClickListener { onLocate() }
            })
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
        showPlace()
        showSummary()
    }

    fun onLocate() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) locate()
        else requestLocation.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
    }

    // Con la precisa, el GPS; solo con la aproximada, la red.
    private fun locate() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) return
        val precise = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        val provider = if (precise) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        place.setText(R.string.locating)
        LocationManagerCompat.getCurrentLocation(
            getSystemService(LocationManager::class.java), provider, CancellationSignal(), ContextCompat.getMainExecutor(this),
        ) { location ->
            notes.position = location?.let { Position(it.latitude, it.longitude) }
            showPlace()
        }
    }

    private fun showPlace() {
        val position = notes.position
        if (position == null) place.setText(R.string.no_place)
        else place.text = getString(R.string.place, position.latitude.toString(), position.longitude.toString())
    }

    fun add(level: Level) {
        if (notes.add(level) != null) description.setText("")
        showPlace()
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

package dev.bitacora

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import java.io.File

// Una observación y un botón por nivel. Elegir uno la devuelve con ese nivel; volver atrás no devuelve nada.
// La foto la hace la app de la cámara, en un archivo de la Bitácora que le presta el FileProvider.
class ObservationActivity : ComponentActivity() {
    lateinit var observation: Observation
        private set
    lateinit var photoState: TextView
        private set

    private val photoName get() = "${observation.code}.jpg"

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            observation = observation.copy(photo = photoName)
            showPhoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val received = intent.observation() ?: run {
            finish()
            return
        }
        // La cámara suele necesitar tanta memoria que Android mata la Bitácora mientras tanto: la foto ya hecha se
        // guarda en el estado, como el resto de lo que no está en el Intent.
        observation = received.copy(photo = savedInstanceState?.getString(PHOTO) ?: received.photo)
        title = observation.code
        photoState = TextView(this)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = observation.description })
            addView(TextView(context).apply {
                val position = observation.position
                text = if (position == null) getString(R.string.no_place)
                else getString(R.string.place, position.latitude.toString(), position.longitude.toString())
            })
            addView(photoState)
            addView(Button(context).apply {
                setText(R.string.photo)
                setOnClickListener { takePhoto() }
            })
            for (level in Level.entries) {
                addView(Button(context).apply {
                    setText(level.label)
                    setOnClickListener { choose(level) }
                })
            }
        })
        showPhoto()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PHOTO, observation.photo)
    }

    fun takePhoto() {
        val file = File(filesDir, "photos/$photoName").apply { parentFile?.mkdirs() }
        takePicture.launch(FileProvider.getUriForFile(this, "$packageName.files", file))
    }

    fun choose(level: Level) {
        setResult(RESULT_OK, Intent().putObservation(observation.copy(level = level)))
        finish()
    }

    private fun showPhoto() = photoState.setText(if (observation.photo == null) R.string.without_photo else R.string.with_photo)

    private companion object {
        const val PHOTO = "photo"
    }
}

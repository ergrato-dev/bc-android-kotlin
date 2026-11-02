package dev.bitacora

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import dev.bitacora.core.Observation
import dev.bitacora.data.decode
import dev.bitacora.data.encode

const val EXTRA_OBSERVATION = "dev.bitacora.extra.OBSERVATION"

// La observación viaja como el JSON de data: core no es Parcelable, porque no sabe nada de Android.
fun Intent.putObservation(observation: Observation): Intent = putExtra(EXTRA_OBSERVATION, encode(listOf(observation)))

fun Intent.observation(): Observation? = getStringExtra(EXTRA_OBSERVATION)?.let { decode(it).single() }

// Abre la observación para editarla. Devuelve la observación editada, o null si el usuario volvió sin cambiarla.
class EditObservation : ActivityResultContract<Observation, Observation?>() {
    override fun createIntent(context: Context, input: Observation): Intent =
        Intent(context, ObservationActivity::class.java).putObservation(input)

    override fun parseResult(resultCode: Int, intent: Intent?): Observation? =
        if (resultCode == Activity.RESULT_OK) intent?.observation() else null
}

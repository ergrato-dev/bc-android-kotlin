package dev.bitacora

import android.app.Application
import dev.bitacora.data.JsonFileObservationRepository
import dev.bitacora.data.ObservationRepository
import java.io.File

// Ya está hecho. Paso 5: registrarla en el manifiesto (android:name) y dársela al ViewModel desde MainActivity.
// DI a mano: la aplicación crea una vez lo que dura tanto como ella y se lo da a quien lo pide. Antes de la primera
// observación, las de ejemplo de la variante (Samples).
class BitacoraApp : Application() {
    val repository: ObservationRepository by lazy {
        JsonFileObservationRepository(File(filesDir, "observations.json"), Samples.observations)
    }
}

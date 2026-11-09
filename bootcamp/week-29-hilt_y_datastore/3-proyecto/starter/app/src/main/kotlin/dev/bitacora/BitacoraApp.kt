package dev.bitacora

import android.app.Application
import dev.bitacora.data.DataStoreSettingsRepository
import dev.bitacora.data.JsonFileObservationRepository
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
import java.io.File

// DI a mano (semana 28): la aplicación crea una vez lo que dura tanto como ella y se lo da a quien lo pide. Antes de la
// primera observación, las de ejemplo de la variante (Samples).
// Paso 3: @HiltAndroidApp, y lo que crea pasa a AppModule.
class BitacoraApp : Application() {
    val repository: ObservationRepository by lazy {
        JsonFileObservationRepository(File(filesDir, "observations.json"), Samples.observations)
    }

    val settings: SettingsRepository by lazy {
        DataStoreSettingsRepository(File(filesDir, "datastore/settings.preferences_pb"))
    }
}

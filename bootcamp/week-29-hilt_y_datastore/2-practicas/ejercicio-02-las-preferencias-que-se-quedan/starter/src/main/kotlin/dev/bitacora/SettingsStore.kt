package dev.bitacora

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

// Las preferencias de las salidas. De momento, en memoria: al cerrar la app, se pierden.
// file es donde las guardará DataStore (tiene que acabar en .preferences_pb); scope, donde DataStore lee y escribe;
// legacy, el archivo de la versión anterior (units=..., en formato Properties).
class SettingsStore(file: File, scope: CoroutineScope, legacy: File? = null) {
    // Paso 1: PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }), con una clave por campo.
    // Paso 2: corruptionHandler.
    // Paso 3: migrations, con una DataMigration<Preferences> que lee legacy.
    private val memory = MutableStateFlow(TripSettings())

    val settings: Flow<TripSettings> = memory

    suspend fun setUnits(units: Units) {
        memory.update { it.copy(units = units) }
    }

    suspend fun setReminders(on: Boolean) {
        memory.update { it.copy(reminders = on) }
    }
}

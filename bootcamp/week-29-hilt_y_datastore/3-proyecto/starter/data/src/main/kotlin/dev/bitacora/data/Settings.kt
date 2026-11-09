package dev.bitacora.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

// Cómo quiere ver la persona la lista. No son observaciones: son preferencias, y van aparte, en DataStore.
data class ListSettings(val newestFirst: Boolean = false, val onlyHigh: Boolean = false)

interface SettingsRepository {
    val settings: Flow<ListSettings>

    suspend fun save(settings: ListSettings)
}

// En memoria: al cerrar la app, se pierden.
// Paso 2: DataStore en file (PreferenceDataStoreFactory.create), con scope, una clave por campo y un corruptionHandler.
class DataStoreSettingsRepository(
    file: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : SettingsRepository {
    private val memory = MutableStateFlow(ListSettings())

    override val settings: Flow<ListSettings> = memory

    override suspend fun save(settings: ListSettings) {
        memory.value = settings
    }
}

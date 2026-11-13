package dev.bitacora.data

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

// Cómo quiere ver la persona la lista. No son observaciones: son preferencias, y van aparte, en DataStore.
data class ListSettings(val newestFirst: Boolean = false, val onlyHigh: Boolean = false)

interface SettingsRepository {
    val settings: Flow<ListSettings>

    suspend fun save(settings: ListSettings)
}

private val NEWEST_FIRST = booleanPreferencesKey("newest_first")
private val ONLY_HIGH = booleanPreferencesKey("only_high")

// Un DataStore por archivo y por proceso: quien lo cree tiene que crearlo una sola vez (@Singleton en AppModule).
// Si el archivo está roto, se empieza de cero en vez de fallar.
class DataStoreSettingsRepository(
    file: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : SettingsRepository {
    private val store = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
        produceFile = { file },
    )

    override val settings: Flow<ListSettings> = store.data.map {
        ListSettings(newestFirst = it[NEWEST_FIRST] ?: false, onlyHigh = it[ONLY_HIGH] ?: false)
    }

    override suspend fun save(settings: ListSettings) {
        store.edit {
            it[NEWEST_FIRST] = settings.newestFirst
            it[ONLY_HIGH] = settings.onlyHigh
        }
    }
}

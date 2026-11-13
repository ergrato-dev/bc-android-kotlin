package dev.bitacora

import android.content.Context
import androidx.room3.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.bitacora.data.BitacoraDatabase
import dev.bitacora.data.DataStoreSettingsRepository
import dev.bitacora.data.ObservationApi
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.RoomObservationRepository
import dev.bitacora.data.SettingsRepository
import dev.bitacora.data.bitacora
import dev.bitacora.data.bitacoraClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.io.File
import javax.inject.Singleton

// Lo que antes creaba BitacoraApp. @Singleton: uno para toda la app; sin él, dos DataStore sobre el mismo archivo.
// HiltTest lo cambia por FakeModule.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Una sola base de datos para toda la app: dos instancias sobre el mismo archivo no se avisan de los cambios.
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): BitacoraDatabase =
        Room.databaseBuilder<BitacoraDatabase>(context, context.getDatabasePath("bitacora.db").path)
            .bitacora(File(context.filesDir, "observations.json"), Samples.observations)
            .build()

    // Un cliente para toda la app: OkHttp guarda las conexiones abiertas y las reutiliza. La URL, la del flavor.
    @Provides
    @Singleton
    fun client(): HttpClient = bitacoraClient(OkHttp.create(), BuildConfig.API_URL)

    @Provides
    @Singleton
    // Paso 7: con la sincronización detrás de cada cambio (SyncingRepository y WorkManager.getInstance(context)).
    fun observations(@ApplicationContext context: Context, database: BitacoraDatabase, client: HttpClient): ObservationRepository =
        RoomObservationRepository(database, ObservationApi(client))

    @Provides
    @Singleton
    fun settings(@ApplicationContext context: Context): SettingsRepository =
        DataStoreSettingsRepository(File(context.filesDir, "datastore/settings.preferences_pb"))
}

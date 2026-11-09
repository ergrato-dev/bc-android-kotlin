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
import dev.bitacora.data.JsonFileObservationRepository
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
import dev.bitacora.data.bitacora
import java.io.File
import javax.inject.Singleton

// Lo que antes creaba BitacoraApp. @Singleton: uno para toda la app; sin él, dos DataStore sobre el mismo archivo.
// HiltTest lo cambia por FakeModule.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Ya está hecho: la base de datos, con el builder de Android. Las de partida y el JSON viejo, los de abajo.
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): BitacoraDatabase =
        Room.databaseBuilder<BitacoraDatabase>(context, context.getDatabasePath("bitacora.db").path)
            .bitacora(File(context.filesDir, "observations.json"), Samples.observations)
            .build()

    // Paso 6: que reciba la BitacoraDatabase y devuelva un RoomObservationRepository.
    @Provides
    @Singleton
    fun observations(@ApplicationContext context: Context): ObservationRepository =
        JsonFileObservationRepository(File(context.filesDir, "observations.json"), Samples.observations)

    @Provides
    @Singleton
    fun settings(@ApplicationContext context: Context): SettingsRepository =
        DataStoreSettingsRepository(File(context.filesDir, "datastore/settings.preferences_pb"))
}

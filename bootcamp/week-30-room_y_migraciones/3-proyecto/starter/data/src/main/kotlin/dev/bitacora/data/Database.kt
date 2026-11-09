package dev.bitacora.data

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import dev.bitacora.core.Observation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File

// Una fila de la tabla. Aparte de Observation: core no sabe de Room, y la tabla puede cambiar sin tocar el dominio.
// Paso 2: una columna por campo de Observation (la posición en dos, latitude y longitude, que pueden ser null).
@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey val code: String,
)

@Dao
interface ObservationDao {
    // Paso 3: por el número del código, no por el texto. Y upsert, deleteOthers y replace, con @Transaction.
    @Query("SELECT * FROM observations")
    fun all(): Flow<List<ObservationEntity>>
}

@Database(entities = [ObservationEntity::class], version = 1)
abstract class BitacoraDatabase : RoomDatabase() {
    abstract fun observations(): ObservationDao
}

// Lo que el builder necesita en Android y en la JVM.
// Paso 4: las consultas en Dispatchers.IO (setQueryCoroutineContext) y un RoomDatabase.Callback: en onCreate, lo que
// hay en legacy (o initial, si no hay archivo o está roto); en onOpen, borrar legacy.
@Suppress("UNUSED_PARAMETER")
fun RoomDatabase.Builder<BitacoraDatabase>.bitacora(legacy: File, initial: List<Observation>) = this

// Paso 5: observations desde el DAO y save con replace, traduciendo entre Observation y ObservationEntity.
@Suppress("UNUSED_PARAMETER")
class RoomObservationRepository(database: BitacoraDatabase) : ObservationRepository {
    override val observations: Flow<List<Observation>> = flowOf(emptyList())

    override suspend fun save(observations: List<Observation>) {
    }
}

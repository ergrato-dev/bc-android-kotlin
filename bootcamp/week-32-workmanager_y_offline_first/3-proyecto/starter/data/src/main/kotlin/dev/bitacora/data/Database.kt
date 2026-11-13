package dev.bitacora.data

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.sqlite.SQLiteConnection
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

// Una fila de la tabla. Aparte de Observation: core no sabe de Room, y la tabla puede cambiar sin tocar el dominio.
@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey val code: String,
    val level: Level,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val photo: String?,
    // Paso 2: updatedAt, cuándo cambió por última vez. Las filas de la versión 1 no lo tienen.
)

// Paso 2: PendingChange, la cola: una fila por código (el último cambio sin subir), con deleted y updatedAt.

@Dao
interface ObservationDao {
    // Por el número del código: A-10 después de A-2.
    @Query("SELECT * FROM observations ORDER BY CAST(substr(code, instr(code, '-') + 1) AS INTEGER)")
    fun all(): Flow<List<ObservationEntity>>

    @Upsert
    suspend fun upsert(observations: List<ObservationEntity>)

    @Query("DELETE FROM observations WHERE code NOT IN (:codes)")
    suspend fun deleteOthers(codes: List<String>)

    // Todo o nada: quien escucha no ve la lista a medias.
    // Paso 3: replace(observations, now) también deja en la cola lo que ha cambiado: las nuevas, las distintas (sin
    // contar la hora) y las que ya no están. Las que no han cambiado conservan su hora.
    // Paso 5: merge(received), con «gana el último» frente a la cola.
    @Transaction
    suspend fun replace(observations: List<ObservationEntity>) {
        deleteOthers(observations.map { it.code })
        upsert(observations)
    }
}

// Paso 2: versión 2, con PendingChange y la migración automática desde la 1.
@Database(entities = [ObservationEntity::class], version = 1)
abstract class BitacoraDatabase : RoomDatabase() {
    abstract fun observations(): ObservationDao
}

// Lo que el builder necesita en Android y en la JVM. La primera vez, lo del JSON de la semana 29 (o las de partida);
// después, el archivo sobra.
fun RoomDatabase.Builder<BitacoraDatabase>.bitacora(legacy: File, initial: List<Observation>) =
    setQueryCoroutineContext(Dispatchers.IO)
        .addCallback(object : RoomDatabase.Callback() {
            override suspend fun onCreate(connection: SQLiteConnection) {
                val start = if (!legacy.exists()) initial else try {
                    decode(legacy.readText())
                } catch (_: IllegalArgumentException) {
                    legacy.renameTo(File(legacy.path + ".bad"))
                    initial
                }
                connection.prepare("INSERT INTO observations VALUES (?, ?, ?, ?, ?, ?)").use { insert ->
                    for (o in start) {
                        insert.bindText(1, o.code)
                        insert.bindText(2, o.level.name)
                        insert.bindText(3, o.description)
                        o.position?.let { insert.bindDouble(4, it.latitude) } ?: insert.bindNull(4)
                        o.position?.let { insert.bindDouble(5, it.longitude) } ?: insert.bindNull(5)
                        o.photo?.let { insert.bindText(6, it) } ?: insert.bindNull(6)
                        insert.step()
                        insert.reset()
                    }
                }
            }

            // Después de onCreate: si el proceso muere a mitad de la importación, el archivo sigue ahí.
            override suspend fun onOpen(connection: SQLiteConnection) {
                legacy.delete()
            }
        })

private fun ObservationEntity.toObservation() = Observation(
    code, level, description,
    if (latitude != null && longitude != null) Position(latitude, longitude) else null,
    photo,
)

private fun Observation.toEntity() =
    ObservationEntity(code, level, description, position?.latitude, position?.longitude, photo)

// Del servidor a la tabla. Un nivel que la app no conoce (una versión nueva del servidor) da null y se salta. La foto
// no viaja: es un archivo de este móvil.
// Paso 4: con updatedAt, y la vuelta, ObservationEntity.toRemote(), con el nivel en minúsculas.
private fun RemoteObservation.toEntity(): ObservationEntity? {
    val level = Level.entries.find { it.name.equals(level, ignoreCase = true) } ?: return null
    return ObservationEntity(code, level, description, latitude, longitude, null)
}

// La red no le habla a la pantalla: lo que llega se escribe en la base de datos, y el Flow de siempre lo enseña. El
// reloj, de fuera: los tests deciden qué hora es.
class RoomObservationRepository(
    database: BitacoraDatabase,
    private val api: ObservationApi,
    private val clock: () -> Long = System::currentTimeMillis,
) : ObservationRepository {
    private val dao = database.observations()

    override val observations: Flow<List<Observation>> = dao.all().map { list -> list.map { it.toObservation() } }

    // Paso 3: con la hora de clock().
    override suspend fun save(observations: List<Observation>) = dao.replace(observations.map { it.toEntity() })

    // Las del servidor pisan a las de aquí con el mismo código; las que solo están aquí se quedan.
    // Paso 5: primero bajar (merge), después subir la cola. 409 y 404 sacan el cambio de la cola; cualquier otro fallo
    // de la red para todo y la deja como estaba. La cancelación sigue subiendo.
    override suspend fun refresh(): RefreshResult {
        val received = try {
            api.observations().mapNotNull { it.toEntity() }
        } catch (e: Exception) {
            return RefreshResult.Failed(e.toNetworkError() ?: throw e)
        }
        dao.upsert(received)
        return RefreshResult.Done(received.size)
    }
}

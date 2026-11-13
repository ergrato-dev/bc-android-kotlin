package dev.bitacora.data

import androidx.room3.AutoMigration
import androidx.room3.ColumnInfo
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
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
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
    // Semana 32: cuándo cambió por última vez, aquí o en el servidor (milisegundos desde 1970). Decide quién gana en un
    // conflicto. El valor por defecto es para las filas de la versión 1: sin él, la migración no sabría qué poner.
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0,
)

// Un cambio hecho aquí que el servidor aún no tiene: la cola de la sincronización. Una fila por código, el último
// cambio; un borrado es una fila con deleted = true (la observación ya no está para saber que se borró).
@Entity(tableName = "pending")
data class PendingChange(
    @PrimaryKey val code: String,
    val deleted: Boolean,
    val updatedAt: Long,
)

@Dao
interface ObservationDao {
    // Por el número del código: A-10 después de A-2.
    @Query("SELECT * FROM observations ORDER BY CAST(substr(code, instr(code, '-') + 1) AS INTEGER)")
    fun all(): Flow<List<ObservationEntity>>

    @Upsert
    suspend fun upsert(observations: List<ObservationEntity>)

    @Query("DELETE FROM observations WHERE code NOT IN (:codes)")
    suspend fun deleteOthers(codes: List<String>)

    @Query("SELECT * FROM observations")
    suspend fun snapshot(): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE code = :code")
    suspend fun find(code: String): ObservationEntity?

    @Query("SELECT * FROM pending ORDER BY updatedAt, code")
    suspend fun pending(): List<PendingChange>

    @Upsert
    suspend fun queue(changes: List<PendingChange>)

    @Query("DELETE FROM pending WHERE code IN (:codes)")
    suspend fun unqueue(codes: List<String>)

    // Solo si sigue siendo ese cambio: si se ha vuelto a cambiar mientras se subía, el nuevo se queda en la cola.
    @Query("DELETE FROM pending WHERE code = :code AND updatedAt = :updatedAt")
    suspend fun sent(code: String, updatedAt: Long)

    // La lista nueva entera, y en la cola, lo que ha cambiado: las nuevas, las que no son iguales (sin contar la hora)
    // y las que ya no están. Las que no han cambiado conservan su hora. Todo o nada: quien escucha no ve la lista a
    // medias, y no hay cambio sin su fila en la cola.
    @Transaction
    suspend fun replace(observations: List<ObservationEntity>, now: Long) {
        val before = snapshot().associateBy { it.code }
        val after = observations.map { new ->
            val old = before[new.code]
            if (old != null && old.copy(updatedAt = 0) == new.copy(updatedAt = 0)) old else new.copy(updatedAt = now)
        }
        val gone = before.keys - observations.map { it.code }.toSet()
        deleteOthers(observations.map { it.code })
        upsert(after)
        queue(after.filter { it.updatedAt == now }.map { PendingChange(it.code, false, now) } + gone.map { PendingChange(it, true, now) })
    }

    // Lo que llega del servidor, con la regla «gana el último»: si aquí hay un cambio sin subir más nuevo (o de la misma
    // hora), se queda el de aquí; si el del servidor es más nuevo, entra y el de aquí sale de la cola sin subirse.
    @Transaction
    suspend fun merge(received: List<ObservationEntity>) {
        val waiting = pending().associateBy { it.code }
        val newer = received.filter { remote -> waiting[remote.code].let { it == null || it.updatedAt < remote.updatedAt } }
        upsert(newer)
        unqueue(newer.map { it.code }.filter { it in waiting })
    }
}

// Semana 32: versión 2, con updatedAt y la cola. Room escribe la migración (columna nueva con valor por defecto, tabla
// nueva) comparando schemas/1.json con schemas/2.json.
@Database(
    entities = [ObservationEntity::class, PendingChange::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
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
                connection.prepare("INSERT INTO observations (code, level, description, latitude, longitude, photo) VALUES (?, ?, ?, ?, ?, ?)").use { insert ->
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
private fun RemoteObservation.toEntity(): ObservationEntity? {
    val level = Level.entries.find { it.name.equals(level, ignoreCase = true) } ?: return null
    return ObservationEntity(code, level, description, latitude, longitude, null, updatedAt)
}

private fun ObservationEntity.toRemote() =
    RemoteObservation(code, level.name.lowercase(), description, latitude, longitude, updatedAt)

// La red no le habla a la pantalla: lo que llega se escribe en la base de datos, y el Flow de siempre lo enseña. El
// reloj, de fuera: los tests deciden qué hora es.
class RoomObservationRepository(
    database: BitacoraDatabase,
    private val api: ObservationApi,
    private val clock: () -> Long = System::currentTimeMillis,
) : ObservationRepository {
    private val dao = database.observations()

    override val observations: Flow<List<Observation>> = dao.all().map { list -> list.map { it.toObservation() } }

    // Guardar no espera a la red: la base de datos cambia ya, y lo que hay que subir queda en la cola.
    override suspend fun save(observations: List<Observation>) = dao.replace(observations.map { it.toEntity() }, clock())

    // Primero bajar, después subir. Al bajar, «gana el último» decide qué se queda; lo que queda en la cola es lo que
    // gana aquí, y se sube. Un cambio que el servidor rechaza porque tiene uno más nuevo (409), o un borrado de algo
    // que ya no tiene (404), sale de la cola: el suyo llega en la próxima. Cualquier otro fallo de la red para todo y
    // deja la cola como estaba; la cancelación (y lo que no es de la red) sigue subiendo.
    override suspend fun refresh(): RefreshResult = try {
        val received = api.observations().mapNotNull { it.toEntity() }
        dao.merge(received)
        for (change in dao.pending()) {
            try {
                if (change.deleted) api.delete(change.code, change.updatedAt) else dao.find(change.code)?.let { api.put(it.toRemote()) }
            } catch (e: ClientRequestException) {
                if (e.response.status != HttpStatusCode.Conflict && e.response.status != HttpStatusCode.NotFound) throw e
            }
            dao.sent(change.code, change.updatedAt)
        }
        RefreshResult.Done(received.size)
    } catch (e: Exception) {
        RefreshResult.Failed(e.toNetworkError() ?: throw e)
    }
}

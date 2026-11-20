package dev.bitacora.data

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import dev.bitacora.database.BitacoraDatabase
import dev.bitacora.database.ObservationEntity
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.model.Position
import dev.bitacora.network.ObservationApi
import dev.bitacora.network.RemoteObservation
import dev.bitacora.network.toNetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

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

// latitude y longitude son de otro módulo: el compilador no puede suponer que no cambian entre la comprobación y el uso,
// y no hace el smart cast. Copiadas a un val local, sí.
private fun ObservationEntity.toObservation(): Observation {
    val latitude = latitude
    val longitude = longitude
    return Observation(code, level, description, if (latitude != null && longitude != null) Position(latitude, longitude) else null, photo)
}

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

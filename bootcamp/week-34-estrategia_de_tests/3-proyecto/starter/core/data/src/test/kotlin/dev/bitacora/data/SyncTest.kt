package dev.bitacora.data

import dev.bitacora.database.BitacoraDatabase
import dev.bitacora.network.NetworkError
import dev.bitacora.network.ObservationApi
import dev.bitacora.network.bitacoraClient

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

// Semana 32: la cola de cambios y la sincronización. El reloj es del test (now) y el servidor, un MockEngine que
// contesta a GET con remote y a PUT y DELETE con status, y apunta cada subida en sent.
@OptIn(ExperimentalCoroutinesApi::class)
class SyncTest {
    @TempDir
    lateinit var dir: File

    private var now = 1_000L
    private var remote = "[]"
    private var status = HttpStatusCode.OK
    private var offline = false
    private val sent = mutableListOf<String>()

    private val a = Observation("A-1", Level.HIGH, "roya en el lote 2")
    private val b = Observation("A-2", Level.LOW, "pulgón")

    private lateinit var database: BitacoraDatabase

    private fun TestScope.repository(): RoomObservationRepository {
        val engine = MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                if (offline) throw IOException("sin red")
                if (request.method == HttpMethod.Get) {
                    respond("""{"observations": $remote}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                } else {
                    sent += "${request.method.value} ${request.url.encodedPathAndQuery} ${request.body.toByteArray().decodeToString()}".trim()
                    respond("", status)
                }
            }
        }
        database = Room.databaseBuilder<BitacoraDatabase>(File(dir, "bitacora.db").path)
            .setDriver(BundledSQLiteDriver())
            .bitacora(File(dir, "observations.json"), emptyList())
            .build()
        backgroundScope.coroutineContext.job.invokeOnCompletion { database.close() }
        return RoomObservationRepository(database, ObservationApi(bitacoraClient(engine, "https://api.bitacora.example/v1/"))) { now }
    }

    // La cola, leída con SQL: la tabla pending, con su código, si es un borrado y la hora del cambio.
    private suspend fun queue(): List<String> = database.useReaderConnection { connection ->
        connection.usePrepared("SELECT code, deleted, updatedAt FROM pending ORDER BY updatedAt, code") { row ->
            buildList { while (row.step()) add("${row.getText(0)} ${if (row.getBoolean(1)) "borrar" else "subir"} ${row.getLong(2)}") }
        }
    }

    @Test
    fun aNewOneIsQueuedWithTheTime() = runTest {
        repository().save(listOf(a))
        assertEquals(listOf("A-1 subir 1000"), queue())
    }

    // Guardar es guardar la lista entera: solo entra en la cola lo que ha cambiado, y lo demás conserva su hora.
    @Test
    fun onlyWhatChangedIsQueued() = runTest {
        val repository = repository()
        repository.save(listOf(a, b))
        repository.refresh()
        now = 2_000
        repository.save(listOf(a, b.copy(description = "pulgón en el lote 4")))
        assertEquals(listOf("A-2 subir 2000"), queue())
    }

    // Un borrado también se sube: una fila con deleted = true, porque la observación ya no está.
    @Test
    fun aDeletionIsQueued() = runTest {
        val repository = repository()
        repository.save(listOf(a, b))
        repository.refresh()
        now = 2_000
        repository.save(listOf(b))
        assertEquals(listOf("A-1 borrar 2000"), queue())
    }

    @Test
    fun refreshUploadsTheQueueAndEmptiesIt() = runTest {
        val repository = repository()
        repository.save(listOf(a, b))
        now = 2_000
        repository.save(listOf(b))
        assertEquals(RefreshResult.Done(0), repository.refresh())
        assertEquals(
            listOf(
                """PUT /v1/observations/A-2 {"code":"A-2","level":"low","description":"pulgón","updated_at":1000}""",
                "DELETE /v1/observations/A-1?updated_at=2000",
            ),
            sent,
        )
        assertEquals(emptyList(), queue())
    }

    // Sin red no se pierde nada: la cola sigue igual, para el próximo intento.
    @Test
    fun withoutNetworkTheQueueStays() = runTest {
        val repository = repository()
        repository.save(listOf(a))
        offline = true
        assertEquals(RefreshResult.Failed(NetworkError.OFFLINE), repository.refresh())
        assertEquals(listOf("A-1 subir 1000"), queue())
    }

    // Gana el último: el cambio de aquí (1000) es más nuevo que el del servidor (500). Se queda y se sube.
    @Test
    fun aNewerLocalChangeWins() = runTest {
        val repository = repository()
        repository.save(listOf(a))
        remote = """[{"code": "A-1", "level": "low", "description": "del equipo", "updated_at": 500}]"""
        repository.refresh()
        assertEquals("roya en el lote 2", repository.observations.first().single().description)
        assertEquals(1, sent.size)
    }

    // Y al revés: el del servidor (1500) es más nuevo. Entra, y el de aquí sale de la cola sin subirse.
    @Test
    fun aNewerRemoteChangeWins() = runTest {
        val repository = repository()
        repository.save(listOf(a))
        remote = """[{"code": "A-1", "level": "low", "description": "del equipo", "updated_at": 1500}]"""
        repository.refresh()
        assertEquals("del equipo", repository.observations.first().single().description)
        assertEquals(emptyList(), sent)
        assertEquals(emptyList(), queue())
    }

    // Un borrado de aquí también compite: el del servidor es más viejo, y la observación no vuelve.
    @Test
    fun anOlderRemoteDoesNotBringBackADeletion() = runTest {
        val repository = repository()
        repository.save(listOf(a))
        repository.refresh()
        now = 2_000
        repository.save(emptyList())
        remote = """[{"code": "A-1", "level": "high", "description": "roya en el lote 2", "updated_at": 1000}]"""
        repository.refresh()
        assertEquals(emptyList(), repository.observations.first())
    }

    // El servidor tiene uno más nuevo que llegó después de bajar (409): el de aquí sale de la cola; el suyo, en la
    // próxima.
    @Test
    fun aConflictLeavesTheQueue() = runTest {
        val repository = repository()
        repository.save(listOf(a))
        status = HttpStatusCode.Conflict
        assertEquals(RefreshResult.Done(0), repository.refresh())
        assertEquals(emptyList(), queue())
    }

    // Un 503 al subir para todo: la cola sigue entera.
    @Test
    fun aServerErrorWhileUploadingKeepsTheQueue() = runTest {
        val repository = repository()
        repository.save(listOf(a, b))
        status = HttpStatusCode.ServiceUnavailable
        assertEquals(RefreshResult.Failed(NetworkError.SERVER), repository.refresh())
        assertEquals(1, sent.size)
        assertEquals(listOf("A-1 subir 1000", "A-2 subir 1000"), queue())
    }
}

package dev.bitacora.data

import dev.bitacora.database.BitacoraDatabase
import dev.bitacora.network.NetworkError
import dev.bitacora.network.ObservationApi
import dev.bitacora.network.bitacoraClient
import dev.bitacora.network.RemoteObservation
import dev.bitacora.network.REQUEST_TIMEOUT

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Semana 31: las observaciones del equipo, del servidor a la base de datos. Sin red: MockEngine contesta desde el
// propio test, y con el dispatcher del test, los tiempos agotados pasan en tiempo virtual.
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkTest {
    @TempDir
    lateinit var dir: File

    private val base = "https://api.bitacora.example/v1/"
    private val requests = mutableListOf<HttpRequestData>()

    private fun TestScope.server(answer: suspend MockRequestHandleScope.() -> HttpResponseData) = bitacoraClient(
        MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                requests += request
                answer()
            }
        },
        base,
    )

    private fun MockRequestHandleScope.json(text: String) =
        respond(text, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

    private val two = """
        {"observations": [
            {"code": "A-1", "level": "high", "description": "roya en el lote 2", "lat": 4.6, "lng": -74.08},
            {"code": "A-2", "level": "low", "description": "pulgón"}
        ]}
    """

    private fun TestScope.repository(answer: suspend MockRequestHandleScope.() -> HttpResponseData): RoomObservationRepository {
        val database = Room.databaseBuilder<BitacoraDatabase>(File(dir, "bitacora.db").path)
            .setDriver(BundledSQLiteDriver())
            .bitacora(File(dir, "observations.json"), emptyList())
            .build()
        backgroundScope.coroutineContext.job.invokeOnCompletion { database.close() }
        return RoomObservationRepository(database, ObservationApi(server(answer)))
    }

    // Semana 32: la API devuelve las clases del servidor; traducirlas es cosa del repositorio. Sin updated_at, 0.
    @Test
    fun theObservationsArrive() = runTest {
        val api = ObservationApi(server { json(two) })
        assertEquals(
            listOf(RemoteObservation("A-1", "high", "roya en el lote 2", 4.6, -74.08), RemoteObservation("A-2", "low", "pulgón")),
            api.observations(),
        )
    }

    // La ruta va detrás de la URL base, con su /v1/, y se pide JSON.
    @Test
    fun theRequestGoesToTheBaseUrl() = runTest {
        ObservationApi(server { json(two) }).observations()
        assertEquals("https://api.bitacora.example/v1/observations", requests.single().url.toString())
        assertEquals("application/json", requests.single().headers[HttpHeaders.Accept])
    }

    // Una versión nueva del servidor añade un campo: la app que ya está instalada sigue leyendo.
    @Test
    fun aNewFieldDoesNotBreakTheApp() = runTest {
        val api = ObservationApi(server { json("""{"observations": [{"code": "A-3", "level": "medium", "description": "riego", "author": "Ana"}], "page": 1}""") })
        assertEquals(listOf(RemoteObservation("A-3", "medium", "riego")), api.observations())
    }

    // Y un nivel que la app no conoce se salta: las demás llegan igual.
    @Test
    fun anUnknownLevelIsSkipped() = runTest {
        val repository = repository { json("""{"observations": [{"code": "A-3", "level": "urgent", "description": "helada"}, {"code": "A-4", "level": "low", "description": "riego"}]}""") }
        assertEquals(RefreshResult.Done(1), repository.refresh())
        assertEquals(listOf("A-4"), repository.observations.first().map { it.code })
    }

    // Las del servidor entran en la base de datos, y el Flow de siempre las enseña. Semana 32: la A-1 de aquí, sin
    // subir, es más nueva que la del servidor (sin updated_at, 0): gana la de aquí. La A-5, que solo está aquí, se queda.
    @Test
    fun refreshWritesToTheDatabase() = runTest {
        val repository = repository { json(two) }
        repository.save(listOf(Observation("A-1", Level.LOW, "vieja"), Observation("A-5", Level.MEDIUM, "solo aquí")))
        assertEquals(RefreshResult.Done(2), repository.refresh())
        assertEquals(
            listOf("A-1 vieja", "A-2 pulgón", "A-5 solo aquí"),
            repository.observations.first().map { "${it.code} ${it.description}" },
        )
    }

    @Test
    fun aServerErrorIsAnError() = runTest {
        val repository = repository { respondError(HttpStatusCode.ServiceUnavailable) }
        repository.save(listOf(Observation("A-5", Level.MEDIUM, "solo aquí")))
        assertEquals(RefreshResult.Failed(NetworkError.SERVER), repository.refresh())
        assertEquals(listOf("A-5"), repository.observations.first().map { it.code })
    }

    // Con expectSuccess, un 404 no se intenta leer como lista.
    @Test
    fun notFoundIsAServerError() = runTest {
        assertEquals(RefreshResult.Failed(NetworkError.SERVER), repository { respondError(HttpStatusCode.NotFound) }.refresh())
    }

    @Test
    fun withoutNetworkItIsOffline() = runTest {
        assertEquals(RefreshResult.Failed(NetworkError.OFFLINE), repository { throw IOException("sin red") }.refresh())
    }

    @Test
    fun aBrokenAnswerIsABadResponse() = runTest {
        assertEquals(RefreshResult.Failed(NetworkError.BAD_RESPONSE), repository { json("""{"observations": [{"code": "A-1",""") }.refresh())
    }

    // REQUEST_TIMEOUT (15 s): a los 14 s llega; a los 16, no. En tiempo virtual: el test tarda milisegundos.
    @Test
    fun aSlowServerTimesOut() = runTest {
        assertEquals(RefreshResult.Done(2), repository { delay(14_000); json(two) }.refresh())
        val start = currentTime
        assertEquals(RefreshResult.Failed(NetworkError.TIMEOUT), repository { delay(16_000); json(two) }.refresh())
        assertEquals(REQUEST_TIMEOUT, currentTime - start)
    }

    // Cerrar la sesión cancela la petición: no es un error de la red. Si refresh tragase la cancelación, el código de
    // después (el aviso en la pantalla) seguiría como si nada.
    @Test
    fun cancellingIsNotAnError() = runTest {
        val repository = repository { delay(5_000); json(two) }
        var after = false
        val refresh = launch {
            repository.refresh()
            after = true
        }
        runCurrent()
        refresh.cancel()
        advanceUntilIdle()
        assertFalse(after)
    }
}

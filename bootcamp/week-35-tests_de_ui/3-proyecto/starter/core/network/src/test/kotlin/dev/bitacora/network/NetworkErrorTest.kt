package dev.bitacora.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Semana 34: la red, probada en su módulo. Hasta ahora solo la probaban los tests de :core:data, y el informe de Kover
// del raíz la daba por cubierta; el de este módulo, solo, daba 0 líneas.
class NetworkErrorTest {
    @Test
    fun withoutNetworkItIsOffline() = assertEquals(NetworkError.OFFLINE, IOException("sin red").toNetworkError())

    // ConnectTimeoutException también es una IOException: si el when mirara IOException antes, sería OFFLINE.
    @Test
    fun aTimeoutIsATimeoutAndNotOffline() =
        assertEquals(NetworkError.TIMEOUT, ConnectTimeoutException("https://api.bitacora.example/v1/").toNetworkError())

    // Un fallo nuestro no es de la red: null, y la excepción sigue subiendo.
    @Test
    fun aBugIsNotANetworkError() = assertNull(IllegalStateException("fallo nuestro").toNetworkError())

    // La cancelación tampoco: tragársela rompería la concurrencia estructurada.
    @Test
    fun cancellingIsNotANetworkError() = assertNull(CancellationException("cerrada").toNetworkError())

    // El JSON se queda dentro del módulo: fuera llega la lista, sin ObservationsResponse, y un campo nuevo no rompe nada.
    @Test
    fun theListComesOutOfTheResponse() = runTest {
        val engine = MockEngine {
            respond(
                """{"observations": [{"code": "A-1", "level": "high", "description": "roya", "lat": 4.6, "lng": -74.08}], "next": null}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val list = ObservationApi(bitacoraClient(engine, "https://api.bitacora.example/v1/")).observations()
        assertEquals(listOf("A-1"), list.map { it.code })
        assertEquals(4.6, list.single().latitude)
    }
}

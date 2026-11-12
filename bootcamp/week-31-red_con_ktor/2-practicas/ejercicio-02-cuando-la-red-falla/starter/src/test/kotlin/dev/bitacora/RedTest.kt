package dev.bitacora

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// MockEngine con el dispatcher del test: los delay del servidor, los reintentos y el tiempo agotado pasan en tiempo
// virtual. currentTime dice cuándo, sin esperar.
@OptIn(ExperimentalCoroutinesApi::class)
class RedTest {
    private val calls = mutableListOf<Long>()

    private fun TestScope.api(answer: suspend MockRequestHandleScope.(call: Int) -> HttpResponseData) = StationApi(
        stationClient(
            MockEngine.create {
                dispatcher = StandardTestDispatcher(testScheduler)
                addHandler {
                    calls += currentTime
                    answer(calls.size)
                }
            },
        ),
    )

    private fun MockRequestHandleScope.json(text: String) =
        respond(text, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

    private val lote2 = """{"id": "lote-2", "temperature": 3.5}"""

    @Test
    fun anAnswerIsOk() = runTest {
        assertEquals(Answer.Ok(Station("lote-2", 3.5)), api { json(lote2) }.station("lote-2"))
    }

    // Un 404 no se arregla repitiendo: una sola llamada.
    @Test
    fun notFoundIsNotRetried() = runTest {
        assertEquals(Answer.Http(404), api { respondError(HttpStatusCode.NotFound) }.station("lote-9"))
        assertEquals(1, calls.size)
    }

    @Test
    fun aBusyServerIsRetried() = runTest {
        val answer = api { call -> if (call < 3) respondError(HttpStatusCode.ServiceUnavailable) else json(lote2) }.station("lote-2")
        assertEquals(Answer.Ok(Station("lote-2", 3.5)), answer)
        assertEquals(3, calls.size)
    }

    // Cada reintento espera el doble que el anterior: el primero, 1 s; el segundo, 2 s más.
    @Test
    fun eachRetryWaitsLonger() = runTest {
        api { respondError(HttpStatusCode.ServiceUnavailable) }.station("lote-2")
        assertEquals(listOf(0L, 1_000L, 3_000L), calls)
    }

    // Después del último reintento, el error del último intento.
    @Test
    fun aServerThatNeverRecoversIsAnError() = runTest {
        assertEquals(Answer.Http(503), api { respondError(HttpStatusCode.ServiceUnavailable) }.station("lote-2"))
        assertEquals(1 + RETRIES, calls.size)
    }

    @Test
    fun aSlowServerIsATimeout() = runTest {
        assertEquals(Answer.Timeout, api { delay(TIMEOUT + 1_000); json(lote2) }.station("lote-2"))
        assertEquals(TIMEOUT, currentTime)
    }

    @Test
    fun withoutNetworkItIsOffline() = runTest {
        assertEquals(Answer.Offline, api { throw IOException("sin red") }.station("lote-2"))
    }

    @Test
    fun aBrokenAnswerIsUnreadable() = runTest {
        assertEquals(Answer.Unreadable, api { json("""{"id": "lote-2", "temperature": "frío"}""") }.station("lote-2"))
    }

    // Cancelar no es un problema de la red: la cancelación sigue subiendo, no se convierte en una respuesta. Si
    // safely la tragase, el código de después seguiría como si nada.
    @Test
    fun cancellingIsNotAnAnswer() = runTest {
        var after = false
        val station = launch {
            api { delay(5_000); json(lote2) }.station("lote-2")
            after = true
        }
        runCurrent()
        station.cancel()
        advanceUntilIdle()
        assertFalse(after)
    }
}

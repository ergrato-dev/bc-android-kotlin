package dev.bitacora.time

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

// No los toques: son el enunciado. Cada test dura milisegundos aunque el código espere segundos, porque runTest usa un
// reloj virtual (currentTime) que salta de un delay al siguiente.
@OptIn(ExperimentalCoroutinesApi::class)
class TimeTest {
    // --- retrying ---

    @Test
    fun aGoodAnswerDoesNotWait() = runTest {
        assertEquals("ok", retrying(times = 3, first = 1_000) { "ok" })
        assertEquals(0, currentTime)
    }

    // Falla, espera 1 s, falla, espera 2 s, contesta: 3 intentos y 3 s de reloj virtual.
    @Test
    fun theWaitDoubles() = runTest {
        var calls = 0
        val answer = retrying(times = 5, first = 1_000) {
            calls++
            if (calls < 3) throw IOException("sin red")
            "ok"
        }
        assertEquals("ok", answer)
        assertEquals(3, calls)
        assertEquals(3_000, currentTime)
    }

    // Tras el último intento no se espera: 1 s + 2 s, no 1 + 2 + 4.
    @Test
    fun theLastErrorComesOut() = runTest {
        val error = assertFailsWith<IOException> { retrying(times = 3, first = 1_000) { throw IOException("sin red") } }
        assertEquals("sin red", error.message)
        assertEquals(3_000, currentTime)
    }

    @Test
    fun otherErrorsAreNotRepeated() = runTest {
        var calls = 0
        assertFailsWith<IllegalStateException> { retrying(times = 3, first = 1_000) { calls++; error("fallo nuestro") } }
        assertEquals(1, calls)
    }

    // A los 1,5 s se ha intentado dos veces (en 0 y en 1 s) y se espera al tercero: cancelar lo para ahí.
    @Test
    fun cancellingStopsTheRetries() = runTest {
        var calls = 0
        val job = launch { retrying(times = 5, first = 1_000) { calls++; throw IOException("sin red") } }
        advanceTimeBy(1_500)
        job.cancel()
        advanceTimeBy(10_000)
        assertEquals(2, calls)
        assertTrue(job.isCancelled)
    }

    // --- searches ---

    // «r», «ro», «roya» con 100 ms entre teclas: una sola búsqueda, 500 ms después de la última tecla.
    @Test
    fun itSearchesWhenTheTypingStops() = runTest {
        val typed = flow {
            emit("r")
            delay(100)
            emit("ro")
            delay(100)
            emit("roya")
            delay(1_000)
        }
        typed.searches(wait = 500).test {
            assertEquals("roya", awaitItem())
            assertEquals(700, currentTime)
            awaitComplete()
        }
    }

    // Borrar una letra y volver a escribirla deja la misma búsqueda: no se repite.
    @Test
    fun theSameSearchIsNotRepeated() = runTest {
        val typed = flow {
            emit("roya")
            delay(1_000)
            emit("roy")
            delay(100)
            emit("roya ")
            delay(1_000)
            emit("pulgón")
        }
        typed.searches(wait = 500).test {
            assertEquals("roya", awaitItem())
            assertEquals("pulgón", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun anEmptyBoxDoesNotSearch() = runTest {
        val typed = flow {
            emit("   ")
            delay(1_000)
            emit("")
        }
        typed.searches(wait = 500).test {
            awaitComplete()
        }
    }

    // --- Poller ---

    // backgroundScope: el bucle no termina nunca, y runTest lo cancela al acabar el test (sin él, el test no acaba).
    @Test
    fun itFetchesEverySecond() = runTest {
        var calls = 0
        val poller = Poller(backgroundScope, every = 1_000) { ++calls }
        assertNull(poller.latest.value)
        poller.start()
        runCurrent()
        assertEquals(1, poller.latest.value)
        advanceTimeBy(2_500)
        assertEquals(3, poller.latest.value)
    }

    @Test
    fun stopStops() = runTest {
        var calls = 0
        val poller = Poller(backgroundScope, every = 1_000) { ++calls }
        poller.start()
        advanceTimeBy(1_500)
        poller.stop()
        advanceTimeBy(10_000)
        assertEquals(2, calls)
    }

    @Test
    fun startingTwiceIsOneLoop() = runTest {
        var calls = 0
        val poller = Poller(backgroundScope, every = 1_000) { ++calls }
        poller.start()
        poller.start()
        advanceTimeBy(2_500)
        assertEquals(3, calls)
    }

    // Con Turbine sobre el StateFlow: null, y después cada resultado.
    @Test
    fun latestShowsEachResult() = runTest {
        var calls = 0
        val poller = Poller(backgroundScope, every = 1_000) { ++calls * 10 }
        poller.latest.test {
            assertNull(awaitItem())
            poller.start()
            assertEquals(10, awaitItem())
            assertEquals(20, awaitItem())
            assertEquals(1_000, currentTime)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

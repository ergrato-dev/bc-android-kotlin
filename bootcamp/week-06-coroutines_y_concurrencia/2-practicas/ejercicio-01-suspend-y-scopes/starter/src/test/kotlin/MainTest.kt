@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun fetchAllRunsInParallelAndKeepsOrder() = runTest {
        val result = fetchAll(listOf(3, 1, 2)) { id -> delay(100); id * 10 }
        assertEquals(listOf(30, 10, 20), result)
        assertEquals(100, currentTime)
    }

    @Test
    fun raceReturnsTheFastestAndCancelsTheOther() = runTest {
        var slowFinished = false
        val winner = race(
            { delay(300); slowFinished = true; "lenta" },
            { delay(100); "rápida" },
        )
        assertEquals("rápida", winner)
        assertEquals(100, currentTime)
        assertTrue(!slowFinished)
    }

    @Test
    fun retryRecoversAndCountsAttempts() = runTest {
        val result = retry(3) { attempt -> if (attempt < 3) error("fallo $attempt") else "ok en $attempt" }
        assertEquals("ok en 3", result)
    }

    @Test
    fun retryGivesUpWithTheLastError() = runTest {
        var calls = 0
        val e = assertFailsWith<IllegalStateException> { retry(3) { calls++; error("fallo $calls") } }
        assertEquals("fallo 3", e.message)
        assertEquals(3, calls)
    }

    @Test
    fun retryDoesNotSwallowCancellation() = runTest {
        var calls = 0
        assertFailsWith<CancellationException> { retry(5) { calls++; throw CancellationException("cancelada") } }
        assertEquals(1, calls)
    }

    @Test
    fun orDefaultFallsBackOnTimeout() = runTest {
        assertEquals("tarde", orDefault(100, "tarde") { delay(500); "a tiempo" })
        assertEquals(100, currentTime)
        assertEquals("a tiempo", orDefault(100, "tarde") { delay(40); "a tiempo" })
        assertEquals(140, currentTime)
    }

    @Test
    fun countdownStopsWhenCancelled() = runTest {
        val ticks = mutableListOf<Int>()
        val job = startCountdown(5, 100) { ticks += it }
        delay(250)
        job.cancelAndJoin()
        assertEquals(listOf(5, 4, 3), ticks)
        val full = mutableListOf<Int>()
        launch { startCountdown(3, 10) { full += it }.join() }.join()
        assertEquals(listOf(3, 2, 1), full)
    }
}

@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun counterDoesNotLoseIncrements() = runTest {
        val counter = SafeCounter()
        withContext(Dispatchers.Default) {
            coroutineScope { repeat(100) { launch { repeat(1000) { counter.increment() } } } }
        }
        assertEquals(100_000, counter.value())
    }

    @Test
    fun channelCarriesTheSquares() = runTest {
        assertEquals(30, sumOfSquares(4))
        assertEquals(0, sumOfSquares(0))
    }

    @Test
    fun oneFailureDoesNotStopTheOthers() = runTest {
        val results = runAll(
            listOf(
                { delay(100); 1 },
                { error("boom") },
                { delay(200); 3 },
            ),
        )
        assertEquals(listOf(1, null, 3), results.map { it.getOrNull() })
        assertEquals("boom", results[1].exceptionOrNull()?.message)
        assertEquals(200, currentTime)
    }

    @Test
    fun resourceIsClosedEvenWhenCancelled() = runTest {
        val log = mutableListOf<String>()
        val job = launch { withResource(log) { delay(1000) } }
        delay(100)
        job.cancelAndJoin()
        assertEquals(listOf("open", "close"), log)
        assertEquals(150, currentTime)
    }

    @Test
    fun limitedMapNeverExceedsTheLimit() = runTest {
        val running = AtomicInteger()
        val peak = AtomicInteger()
        val result = mapLimited(listOf(1, 2, 3, 4, 5, 6), 2) { n ->
            peak.accumulateAndGet(running.incrementAndGet(), ::maxOf)
            delay(100)
            running.decrementAndGet()
            n * 2
        }
        assertEquals(listOf(2, 4, 6, 8, 10, 12), result)
        assertEquals(2, peak.get())
        assertEquals(300, currentTime)
    }

    @Test
    fun cpuLoopNoticesCancellation() = runTest {
        val seen = mutableListOf<Int>()
        lateinit var job: Job
        job = launch { processAll((1..10).toList()) { seen += it; if (it == 3) job.cancel() } }
        job.join()
        assertEquals(listOf(1, 2, 3), seen)
        assertTrue(job.isCancelled)
    }

    @Test
    fun errorScopeCollectsFailures() = runTest {
        val errors = mutableListOf<String>()
        val scope = errorScope(errors)
        scope.launch { error("a") }.join()
        scope.launch { delay(10); error("b") }.join()
        val ok = scope.launch { delay(10) }
        ok.join()
        assertEquals(listOf("a", "b"), errors)
        assertTrue(!ok.isCancelled)
    }
}

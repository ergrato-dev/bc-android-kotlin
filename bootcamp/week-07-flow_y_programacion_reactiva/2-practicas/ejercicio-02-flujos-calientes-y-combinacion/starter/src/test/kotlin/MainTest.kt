@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun stateFlowKeepsTheLatestValueAndIgnoresEqualOnes() = runTest(UnconfinedTestDispatcher()) {
        val counter = Counter()
        assertEquals(0, counter.count.value)
        val seen = mutableListOf<Int>()
        backgroundScope.launch { counter.count.collect { seen += it } }
        counter.set(1)
        counter.set(1)                                  // igual al anterior: no se emite
        counter.increment()
        counter.increment()
        assertEquals(listOf(0, 1, 2, 3), seen)
        assertEquals(3, counter.count.value)
    }

    @Test
    fun updateIsAtomicAcrossThreads() = runTest {
        val counter = Counter()
        withContext(Dispatchers.Default) {
            coroutineScope { repeat(100) { launch { repeat(1000) { counter.increment() } } } }
        }
        assertEquals(100_000, counter.count.value)
    }

    @Test
    fun subscribersOnlySeeWhatIsPublishedWhileSubscribed() = runTest(UnconfinedTestDispatcher()) {
        val bus = EventBus()
        val early = mutableListOf<String>()
        backgroundScope.launch { bus.events.collect { early += it } }
        bus.publish("a")
        val late = mutableListOf<String>()
        backgroundScope.launch { bus.events.collect { late += it } }
        bus.publish("b")
        assertEquals(listOf("a", "b"), early)
        assertEquals(listOf("b"), late)
    }

    @Test
    fun replayGivesLateSubscribersTheLastEvents() = runTest(UnconfinedTestDispatcher()) {
        val bus = EventBus(replay = 2)
        bus.publish("a")
        bus.publish("b")
        bus.publish("c")
        val late = mutableListOf<String>()
        backgroundScope.launch { bus.events.collect { late += it } }
        assertEquals(listOf("b", "c"), late)
    }

    @Test
    fun theSourceRunsOnlyWhileSomeoneListens() = runTest {
        var running = false
        val source = flow {
            running = true
            emit(1)
            delay(Long.MAX_VALUE)
        }.onCompletion { running = false }
        val state = source.shareAsState(backgroundScope, initial = 0)
        runCurrent()
        assertTrue(!running)                            // nadie escucha todavía
        assertEquals(0, state.value)

        val job = launch { state.collect { } }
        runCurrent()
        assertTrue(running)
        assertEquals(1, state.value)

        job.cancel()
        runCurrent()
        advanceTimeBy(4_999)
        assertTrue(running)                             // aún dentro de los 5 000 ms de cortesía
        advanceTimeBy(2)
        runCurrent()
        assertTrue(!running)
    }

    @Test
    fun searchRecalculatesWhenEitherInputChanges() = runTest(UnconfinedTestDispatcher()) {
        val items = MutableStateFlow(listOf("Norte", "Sur", "norte alto"))
        val query = MutableStateFlow("")
        val seen = mutableListOf<List<String>>()
        backgroundScope.launch { searchResults(items, query).collect { seen += it } }
        query.value = "NORTE"
        items.value = listOf("Norte", "Este")
        assertEquals(
            listOf(
                listOf("Norte", "Sur", "norte alto"),
                listOf("Norte", "norte alto"),
                listOf("Norte"),
            ),
            seen,
        )
    }

    @OptIn(FlowPreview::class)
    @Test
    fun settledWaitsForQuietAndDropsRepeats() = runTest {
        val typing = flow {
            emit("a")
            delay(50)
            emit("ab")
            delay(50)
            emit("abc")
            delay(500)
            emit("abc")
            delay(500)
            emit("abd")
        }
        assertEquals(listOf("abc", "abd"), typing.settled(200).toList())
    }

    @Test
    fun searchLatestCancelsTheStaleSearch() = runTest {
        val finished = mutableListOf<String>()
        val queries = flow {
            emit("a")
            delay(100)
            emit("ab")
        }
        val results = searchLatest(queries) { q ->
            delay(if (q == "a") 300 else 50)
            finished += q
            listOf("resultado de $q")
        }.toList()
        assertEquals(listOf(listOf("resultado de ab")), results)
        assertEquals(listOf("ab"), finished)
        assertEquals(150, currentTime)
    }
}

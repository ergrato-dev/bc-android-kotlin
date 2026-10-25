@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun tickerIsColdAndWaitsBeforeEachValue() = runTest {
        val flow = ticker(3, 100)
        assertEquals(0, currentTime)                    // crear el flujo no ejecuta nada
        assertEquals(listOf(1, 2, 3), flow.toList())
        assertEquals(300, currentTime)
        assertEquals(listOf(1, 2, 3), flow.toList())    // otra recogida empieza de cero
        assertEquals(600, currentTime)
    }

    @Test
    fun runningSumAccumulates() = runTest {
        assertEquals(listOf(1, 3, 6, 10), flowOf(1, 2, 3, 4).runningSum().toList())
        assertEquals(emptyList(), flowOf<Int>().runningSum().toList())
    }

    @Test
    fun firstAboveStopsTheSource() = runTest {
        var emitted = 0
        val source = flow {
            for (n in 1..100) {
                emitted++
                emit(n)
            }
        }
        assertEquals(6, firstAbove(source, 5))
        assertEquals(6, emitted)                        // no siguió hasta 100
        assertNull(firstAbove(flowOf(1, 2, 3), 10))
    }

    @Test
    fun uiStateWrapsValuesAndFailures() = runTest {
        assertEquals(
            listOf(UiState.Loading, UiState.Success(1), UiState.Success(2)),
            flowOf(1, 2).asUiState().toList(),
        )
        val failing = flow {
            emit(1)
            error("boom")
        }
        assertEquals(
            listOf(UiState.Loading, UiState.Success(1), UiState.Failure("boom")),
            failing.asUiState().toList(),
        )
    }

    @Test
    fun retryCollectsAgainFromTheStart() = runTest {
        var attempts = 0
        val flaky = flow {
            attempts++
            emit("a")
            if (attempts < 3) error("fallo $attempts")
            emit("b")
        }
        assertEquals(listOf("a", "a", "a", "b"), flaky.retryTimes(2).toList())
        assertEquals(3, attempts)

        var calls = 0
        val broken = flow<Int> { calls++; error("siempre") }
        val e = assertFailsWith<IllegalStateException> { broken.retryTimes(2).toList() }
        assertEquals("siempre", e.message)
        assertEquals(3, calls)
    }

    @Test
    fun loggedRecordsTheLifecycleWithoutSwallowingErrors() = runTest {
        val ok = mutableListOf<String>()
        flowOf(7, 8).logged(ok).toList()
        assertEquals(listOf("start", "item:7", "item:8", "end:ok"), ok)

        val bad = mutableListOf<String>()
        val failing = flow {
            emit(1)
            error("boom")
        }
        assertFailsWith<IllegalStateException> { failing.logged(bad).toList() }
        assertEquals(listOf("start", "item:1", "end:boom"), bad)
    }

    @Test
    fun flowOnMovesTheBodyToAnotherDispatcher() = runTest {
        val name = workerThreadName().toList().single()
        assertTrue(name.startsWith("DefaultDispatcher-worker"), "se ejecutó en $name")
    }
}

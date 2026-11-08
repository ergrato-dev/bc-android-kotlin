package dev.bitacora

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Ya está hecho: lo que pasa con las otras dos formas de mandar un efecto (teoría 03). Están en verde desde el
// principio: son la medida, no la tarea.
@OptIn(ExperimentalCoroutinesApi::class)
class AlternativesTest {
    // Un SharedFlow sin replay acepta el efecto sin nadie escuchando (tryEmit da true) y lo tira.
    @Test
    fun aSharedFlowLosesWhatNobodyHears() = runTest {
        val effects = MutableSharedFlow<ChecklistEffect>(extraBufferCapacity = 1)
        assertTrue(effects.tryEmit(ChecklistEffect.AllPacked))
        assertNull(withTimeoutOrNull(100) { effects.first() })
    }

    // Un StateFlow junta los valores iguales: dos "todo listo" seguidos son uno, y el segundo no llega.
    @Test
    fun aStateFlowMergesEqualEffects() = runTest {
        val effect = MutableStateFlow<ChecklistEffect?>(null)
        val seen = mutableListOf<ChecklistEffect?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { effect.toList(seen) }
        effect.value = ChecklistEffect.AllPacked
        effect.value = ChecklistEffect.AllPacked
        assertEquals(listOf<ChecklistEffect?>(null, ChecklistEffect.AllPacked), seen)
    }
}

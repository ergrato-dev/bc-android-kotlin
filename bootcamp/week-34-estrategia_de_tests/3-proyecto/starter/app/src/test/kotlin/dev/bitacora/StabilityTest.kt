package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Cada tecla cambia el borrador, y el estado se vuelve a calcular. Las observaciones no han cambiado: tienen que ser
// las mismas instancias, no otras iguales. Si no, cada fila se recompone con cada tecla.
@RunWith(AndroidJUnit4::class)
class StabilityTest {
    @Test
    fun typingKeepsTheSameObservations() {
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(), FakeSettings())
        notes.onIntent(BitacoraIntent.EditDraft("riego"))
        notes.onIntent(BitacoraIntent.Add(Level.HIGH))
        val before = notes.state.value.observations
        notes.onIntent(BitacoraIntent.EditDraft("h"))
        val after = notes.state.value
        // El estado es nuevo (lleva el borrador nuevo), pero la lista es la misma.
        assertEquals("h", after.draft)
        assertSame(before, after.observations)
    }
}

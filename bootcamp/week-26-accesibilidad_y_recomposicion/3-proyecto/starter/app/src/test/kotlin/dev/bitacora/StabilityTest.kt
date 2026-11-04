package dev.bitacora

import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.bitacora.core.Level
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Cada tecla cambia el borrador, y el estado se vuelve a calcular. Las observaciones no han cambiado: tienen que ser
// las mismas instancias, no otras iguales sacadas otra vez del JSON. Si no, cada fila se recompone con cada tecla.
@RunWith(AndroidJUnit4::class)
class StabilityTest {
    @Test
    fun typingKeepsTheSameObservations() {
        val notes = ObservationDraftViewModel(SavedStateHandle())
        // state solo se calcula con alguien suscrito (WhileSubscribed).
        notes.viewModelScope.launch { notes.state.collect {} }
        notes.onDraftChange("riego")
        notes.add(Level.HIGH)
        shadowOf(Looper.getMainLooper()).idle()
        val before = notes.state.value.observations
        notes.onDraftChange("h")
        shadowOf(Looper.getMainLooper()).idle()
        val after = notes.state.value
        // El estado es nuevo (lleva el borrador nuevo), pero la observación es la misma.
        assertEquals("h", after.draft)
        assertSame(before.last(), after.observations.last())
    }
}

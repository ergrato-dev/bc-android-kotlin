package dev.bitacora

import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.model.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// Semana 28: el reducer es una función pura. Ni Robolectric ni corrutinas: un estado, un intent, y se mira lo que sale.
class ReducerTest {
    private val riego = Observation("A-1", Level.HIGH, "riego roto")
    private val hojas = Observation("A-2", Level.MEDIUM, "hojas amarillas")
    private val session = BitacoraUiState(observations = listOf(riego, hojas))

    // Añadir usa el borrador sin espacios y la posición, y deja el formulario en blanco.
    @Test
    fun addTakesTheDraftAndThePosition() {
        val state = session.copy(draft = " pulgón ", position = Position(1.0, 2.0), locating = Locating.IDLE)
        val (after, effect) = reduce(state, BitacoraIntent.Add(Level.LOW))
        assertEquals(Observation("A-3", Level.LOW, "pulgón", Position(1.0, 2.0)), after.observations.last())
        assertEquals("", after.draft)
        assertNull(after.position)
        assertNull(effect)
    }

    // Con el borrador vacío no pasa nada: el mismo estado, no uno igual.
    @Test
    fun addWithoutADraftChangesNothing() = assertSame(session, reduce(session, BitacoraIntent.Add(Level.LOW)).state)

    // Borrar quita la fila, recuerda dónde estaba y pide el aviso, una vez.
    @Test
    fun deleteRemembersWhereItWas() {
        val (after, effect) = reduce(session, BitacoraIntent.Delete("A-1"))
        assertEquals(listOf(hojas), after.observations)
        assertEquals(Deleted(riego, 0), after.deleted)
        assertEquals(BitacoraEffect.ShowUndo("A-1"), effect)
    }

    // Deshacer la devuelve a su sitio, no al final.
    @Test
    fun undoPutsItBack() {
        val deleted = reduce(session, BitacoraIntent.Delete("A-1")).state
        assertEquals(listOf(hojas), deleted.observations)
        assertEquals(listOf(riego, hojas), reduce(deleted, BitacoraIntent.Undo).state.observations)
    }

    // Un código que no existe: ni cambio ni aviso.
    @Test
    fun deletingAMissingOneDoesNothing() = assertEquals(Next(session), reduce(session, BitacoraIntent.Delete("A-9")))

    // Compartir no cambia el estado: solo pide el efecto, con la lista de ese momento.
    @Test
    fun shareIsOnlyAnEffect() = assertEquals(Next(session, BitacoraEffect.Share(listOf(riego, hojas))), reduce(session, BitacoraIntent.Share))
}

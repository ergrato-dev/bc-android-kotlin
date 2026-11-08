package dev.bitacora

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ReducerTest {
    private val agua = Item("agua")
    private val gps = Item("GPS")
    private val list = ChecklistState(items = listOf(agua, gps))

    @Test
    fun addTrimsTheDraftAndClearsIt() {
        val (after, effect) = reduce(ChecklistState(draft = "  libreta "), ChecklistIntent.Add)
        assertEquals(ChecklistState(items = listOf(Item("libreta"))), after)
        assertNull(effect)
    }

    // Nada que añadir: el mismo estado, no uno igual. Compose se salta lo que no ha cambiado (semana 26).
    @Test
    fun anEmptyDraftReturnsTheSameState() {
        val state = list.copy(draft = "   ")
        assertSame(state, reduce(state, ChecklistIntent.Add).state)
    }

    // "gps" y "GPS" son lo mismo.
    @Test
    fun aRepeatedNameIsNotAdded() {
        val state = list.copy(draft = "gps")
        assertSame(state, reduce(state, ChecklistIntent.Add).state)
    }

    @Test
    fun toggleMarksOnlyThatOne() =
        assertEquals(listOf(agua.copy(done = true), gps), reduce(list, ChecklistIntent.Toggle("agua")).state.items)

    // Marcar la última pide el aviso una vez; con todo ya marcado, desmarcar no lo pide.
    @Test
    fun theLastOneAsksForAllPacked() {
        val one = reduce(list, ChecklistIntent.Toggle("agua")).state
        assertEquals(ChecklistEffect.AllPacked, reduce(one, ChecklistIntent.Toggle("GPS")).effect)
        assertNull(reduce(one, ChecklistIntent.Toggle("agua")).effect)
    }

    @Test
    fun removeOffersUndo() {
        val (after, effect) = reduce(list, ChecklistIntent.Remove("agua"))
        assertEquals(listOf(gps), after.items)
        assertEquals(Removed(agua, 0), after.removed)
        assertEquals(ChecklistEffect.OfferUndo("agua"), effect)
    }

    // Deshacer la devuelve a su sitio, no al final, y olvida la quitada: deshacer dos veces no la duplica.
    @Test
    fun undoPutsItBackOnce() {
        val removed = reduce(list, ChecklistIntent.Remove("agua")).state
        assertEquals(listOf(gps), removed.items)
        val undone = reduce(removed, ChecklistIntent.Undo).state
        assertEquals(listOf(agua, gps), undone.items)
        assertSame(undone, reduce(undone, ChecklistIntent.Undo).state)
    }

    @Test
    fun clearDoneKeepsTheRest() {
        val state = list.copy(items = listOf(agua.copy(done = true), gps))
        assertEquals(listOf(gps), reduce(state, ChecklistIntent.ClearDone).state.items)
    }

    // Un nombre que no está: ni cambio ni efecto.
    @Test
    fun unknownNamesChangeNothing() {
        assertEquals(Next(list), reduce(list, ChecklistIntent.Toggle("brújula")))
        assertEquals(Next(list), reduce(list, ChecklistIntent.Remove("brújula")))
    }
}

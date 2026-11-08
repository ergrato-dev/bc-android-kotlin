package dev.bitacora

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val agua = Item("agua")

    @BeforeTest
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun reset() = Dispatchers.resetMain()

    private fun ChecklistViewModel.add(name: String) {
        onIntent(ChecklistIntent.EditDraft(name))
        onIntent(ChecklistIntent.Add)
    }

    @Test
    fun theListComesFromTheRepository() =
        assertEquals(listOf(agua), ChecklistViewModel(InMemoryChecklistRepository(listOf(agua))).state.value.items)

    // Escribir no guarda; añadir, sí: una vez.
    @Test
    fun onlyChangesToTheListAreSaved() {
        val repository = InMemoryChecklistRepository()
        ChecklistViewModel(repository).add("agua")
        assertEquals(1, repository.saves)
        assertEquals(listOf(agua), repository.items.value)
    }

    // Nadie escucha cuando se quita: el aviso espera en el Channel.
    @Test
    fun aMessageWaitsForItsCollector() = runTest {
        val notes = ChecklistViewModel(InMemoryChecklistRepository(listOf(agua)))
        notes.onIntent(ChecklistIntent.Remove("agua"))
        assertEquals(ChecklistMessage.Effect(ChecklistEffect.OfferUndo("agua")), notes.messages.first())
    }

    // Recogido una vez, ya no está: el segundo que mira no lo ve.
    @Test
    fun aMessageIsDeliveredOnce() = runTest {
        val notes = ChecklistViewModel(InMemoryChecklistRepository(listOf(agua)))
        notes.onIntent(ChecklistIntent.Remove("agua"))
        notes.messages.first()
        assertNull(withTimeoutOrNull(100) { notes.messages.first() })
    }

    // Con dos a la vez, cada aviso le llega a uno solo: un Channel reparte, no difunde.
    @Test
    fun twoCollectorsSplitTheMessages() = runTest {
        val notes = ChecklistViewModel(InMemoryChecklistRepository(listOf(agua, Item("GPS"))))
        val first = mutableListOf<ChecklistMessage>()
        val second = mutableListOf<ChecklistMessage>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { notes.messages.toList(first) }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { notes.messages.toList(second) }
        notes.onIntent(ChecklistIntent.Remove("agua"))
        notes.onIntent(ChecklistIntent.Remove("GPS"))
        assertEquals(2, first.size + second.size)
        assertEquals(1, first.size)
    }

    // El disco falla: la lista vuelve a lo guardado y llega SaveFailed, después del aviso de quitar.
    @Test
    fun aFailedSaveRollsBack() = runTest {
        val repository = InMemoryChecklistRepository(listOf(agua)).apply { failing = true }
        val notes = ChecklistViewModel(repository)
        notes.onIntent(ChecklistIntent.Remove("agua"))
        assertEquals(listOf(agua), notes.state.value.items)
        assertEquals(ChecklistMessage.Effect(ChecklistEffect.OfferUndo("agua")), notes.messages.first())
        assertEquals(ChecklistMessage.SaveFailed, notes.messages.first())
    }

    // Cada ViewModel con su repositorio: lo que se inyecta por el constructor no se comparte por accidente.
    @Test
    fun eachViewModelHasItsRepository() {
        val one = InMemoryChecklistRepository()
        val other = InMemoryChecklistRepository()
        ChecklistViewModel(one).add("agua")
        ChecklistViewModel(other)
        assertEquals(emptyList(), other.items.value)
        assertEquals(listOf(agua), one.items.value)
    }
}

package dev.bitacora

import dev.bitacora.data.testing.FakeRepository
import dev.bitacora.data.testing.FakeSettings
import androidx.lifecycle.SavedStateHandle
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import app.cash.turbine.test
import dev.bitacora.data.RefreshResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 28: el ViewModel con un repositorio de mentira, en la JVM. viewModelScope usa Dispatchers.Main: en los tests,
// uno de pruebas que ejecuta en el momento.
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val riego = Observation("A-1", Level.HIGH, "riego roto")

    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun reset() = Dispatchers.resetMain()

    // Las observaciones vienen del repositorio, no de Samples.
    @Test
    fun theListComesFromTheRepository() {
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(listOf(riego)), FakeSettings())
        assertEquals(listOf(riego), notes.state.value.observations)
    }

    // Escribir no toca el repositorio: solo se guarda cuando cambia la lista.
    @Test
    fun onlyChangesToTheListAreSaved() {
        val repository = FakeRepository()
        val notes = BitacoraViewModel(SavedStateHandle(), repository, FakeSettings())
        notes.onIntent(BitacoraIntent.EditDraft("riego"))
        notes.onIntent(BitacoraIntent.EditDraft("riego roto"))
        notes.onIntent(BitacoraIntent.Add(Level.HIGH))
        assertEquals(1, repository.saves)
    }

    // El efecto espera a quien lo recoja: borrar sin nadie escuchando y recogerlo después.
    @Test
    fun anEffectWaitsForItsCollector() = runTest {
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(listOf(riego)), FakeSettings())
        notes.onIntent(BitacoraIntent.Delete("A-1"))
        assertEquals(BitacoraEffect.ShowUndo("A-1"), notes.effect.first())
    }

    // Lo que sobrevive a la muerte del proceso es el SavedStateHandle: el borrador vuelve con un ViewModel nuevo.
    @Test
    fun theDraftSurvivesInTheHandle() {
        val handle = SavedStateHandle()
        BitacoraViewModel(handle, FakeRepository(), FakeSettings()).onIntent(BitacoraIntent.EditDraft("pulgón"))
        assertEquals("pulgón", BitacoraViewModel(handle, FakeRepository(), FakeSettings()).state.value.draft)
    }

    // Semana 34: el estado paso a paso, con Turbine. Cada awaitItem es el siguiente valor; si llegara uno de más (o de
    // menos), el bloque falla al salir.
    @Test
    fun refreshingGoesOnAndOff() = runTest {
        val repository = FakeRepository().apply { answer.value = null }
        val notes = BitacoraViewModel(SavedStateHandle(), repository, FakeSettings())
        notes.state.map { it.refreshing }.test {
            assertFalse(awaitItem())
            notes.onIntent(BitacoraIntent.Refresh)
            assertTrue(awaitItem())
            repository.answer.value = RefreshResult.Done(2)
            assertFalse(awaitItem())
        }
    }

    // El efecto llega una vez y no se repite: expectNoEvents falla si hay algo más esperando.
    @Test
    fun theEffectArrivesOnce() = runTest {
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(listOf(riego)), FakeSettings())
        notes.effect.test {
            notes.onIntent(BitacoraIntent.Delete("A-1"))
            assertEquals(BitacoraEffect.ShowUndo("A-1"), awaitItem())
            expectNoEvents()
        }
    }
}

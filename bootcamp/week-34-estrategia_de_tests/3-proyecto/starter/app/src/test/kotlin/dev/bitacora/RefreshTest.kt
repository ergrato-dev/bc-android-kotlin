package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import dev.bitacora.network.NetworkError
import dev.bitacora.data.RefreshResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 31: pedir las del equipo, desde el ViewModel. El repositorio contesta lo que diga el test, sin red.
@OptIn(ExperimentalCoroutinesApi::class)
class RefreshTest {
    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun reset() = Dispatchers.resetMain()

    private val repository = FakeRepository()
    // lazy: después de setMain. Creado antes, viewModelScope no tiene el Main de pruebas y nada corre en el momento.
    private val notes by lazy { BitacoraViewModel(SavedStateHandle(), repository, FakeSettings()) }

    @Test
    fun theResultIsShownOnce() = runTest {
        repository.answer.value = RefreshResult.Failed(NetworkError.OFFLINE)
        notes.onIntent(BitacoraIntent.Refresh)
        // withTimeoutOrNull, en tiempo virtual: si el efecto no llega, null al momento, sin esperar un minuto.
        assertEquals(BitacoraEffect.ShowRefresh(RefreshResult.Failed(NetworkError.OFFLINE)), withTimeoutOrNull(1_000) { notes.effect.first() })
        assertFalse(notes.state.value.refreshing)
    }

    // Mientras el servidor no contesta, refreshing; un segundo toque no pide otra vez.
    @Test
    fun oneRequestAtATime() {
        repository.answer.value = null
        notes.onIntent(BitacoraIntent.Refresh)
        assertTrue(notes.state.value.refreshing)
        notes.onIntent(BitacoraIntent.Refresh)
        assertEquals(1, repository.refreshes)
        repository.answer.value = RefreshResult.Done(2)
        assertFalse(notes.state.value.refreshing)
    }

    // Lo recibido ya está en la base de datos: el ViewModel no guarda la lista otra vez.
    @Test
    fun refreshingDoesNotSaveTheList() {
        notes.onIntent(BitacoraIntent.Refresh)
        assertEquals(0, repository.saves)
    }

    @Test
    fun theReducerOnlyStartsOnce() {
        val refreshing = reduce(BitacoraUiState(), BitacoraIntent.Refresh).state
        assertTrue(refreshing.refreshing)
        assertTrue(reduce(refreshing, BitacoraIntent.Refresh).state === refreshing)
    }
}

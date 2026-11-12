package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.data.ListSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Semana 29: las preferencias de la lista. Las reglas, en el reducer y en visible(); guardarlas, en el ViewModel.
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTest {
    private val riego = Observation("A-1", Level.HIGH, "riego roto")
    private val hojas = Observation("A-2", Level.LOW, "hojas amarillas")
    private val pulgon = Observation("A-3", Level.HIGH, "pulgón")
    private val three = BitacoraUiState(observations = listOf(riego, hojas, pulgon))

    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun reset() = Dispatchers.resetMain()

    @Test
    fun byDefaultTheListAsItIs() {
        assertEquals(listOf(riego, hojas, pulgon), three.visible())
    }

    @Test
    fun newestFirstReversesIt() {
        assertEquals(listOf(pulgon, hojas, riego), three.copy(settings = ListSettings(newestFirst = true)).visible())
    }

    @Test
    fun onlyHighHidesTheRest() {
        assertEquals(listOf(riego, pulgon), three.copy(settings = ListSettings(onlyHigh = true)).visible())
    }

    @Test
    fun bothTogether() {
        assertEquals(listOf(pulgon, riego), three.copy(settings = ListSettings(newestFirst = true, onlyHigh = true)).visible())
    }

    // Cambiar las preferencias no toca las observaciones: la misma lista, que no se vuelve a guardar.
    @Test
    fun changingTheSettingsKeepsTheList() {
        val after = reduce(three, BitacoraIntent.ChangeSettings(ListSettings(onlyHigh = true))).state
        assertEquals(ListSettings(onlyHigh = true), after.settings)
        assertSame(three.observations, after.observations)
    }

    // Borrar con el filtro puesto borra esa y deshacer la devuelve a su sitio en la lista entera.
    @Test
    fun undoWithAFilterPutsItBackInTheWholeList() {
        val filtered = three.copy(settings = ListSettings(onlyHigh = true))
        val deleted = reduce(filtered, BitacoraIntent.Delete("A-3")).state
        assertEquals(listOf(riego), deleted.visible())
        assertEquals(listOf(riego, hojas, pulgon), reduce(deleted, BitacoraIntent.Undo).state.observations)
    }

    @Test
    fun theSettingsComeFromTheirRepository() {
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(), FakeSettings(ListSettings(newestFirst = true)))
        assertEquals(ListSettings(newestFirst = true), notes.state.value.settings)
    }

    @Test
    fun changingThemSavesThem() {
        val settings = FakeSettings()
        val repository = FakeRepository(listOf(riego))
        BitacoraViewModel(SavedStateHandle(), repository, settings).onIntent(BitacoraIntent.ChangeSettings(ListSettings(onlyHigh = true)))
        assertEquals(ListSettings(onlyHigh = true), settings.settings.value)
        assertEquals(0, repository.saves)
    }

    // Paso 9. El filtro no impide añadir: la Baja entra en la lista (A-4) y solo deja de verse.
    @Test
    fun addingALowWithOnlyHighHidesIt() {
        val filtered = three.copy(draft = "riego por goteo", settings = ListSettings(onlyHigh = true))
        val after = reduce(filtered, BitacoraIntent.Add(Level.LOW)).state
        assertEquals(4, after.observations.size)
        assertEquals(listOf(riego, pulgon), after.visible())
    }

    // Paso 9. A-1 pasa a Baja: sigue en la lista entera, y con el filtro solo queda A-3.
    @Test
    fun replacingWithOnlyHighHidesTheReplaced() {
        val filtered = three.copy(settings = ListSettings(onlyHigh = true))
        val after = reduce(filtered, BitacoraIntent.Replace(riego.copy(level = Level.LOW))).state
        assertEquals(listOf(pulgon), after.visible())
    }

    // Paso 9. Los ajustes también pueden cambiar sin un intent (otra pantalla, una migración): el estado los sigue.
    @Test
    fun aChangeFromTheRepositoryReachesTheState() {
        val settings = FakeSettings()
        val notes = BitacoraViewModel(SavedStateHandle(), FakeRepository(), settings)
        settings.settings.value = ListSettings(onlyHigh = true)
        assertEquals(ListSettings(onlyHigh = true), notes.state.value.settings)
    }
}

package dev.bitacora.data

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Semana 28: el repositorio del archivo. Cada test con su carpeta: nada se queda de uno a otro.
class RepositoryTest {
    private val folder: File = Files.createTempDirectory("bitacora").toFile()
    private val file = File(folder, "observations.json")
    private val riego = Observation("A-1", Level.HIGH, "riego roto")

    @Test
    fun withoutAFileItStartsWithTheInitialOnes() = runTest {
        assertEquals(listOf(riego), JsonFileObservationRepository(file, listOf(riego)).observations.first())
    }

    // Otra instancia con el mismo archivo es otra ejecución de la app: lo guardado sigue ahí.
    @Test
    fun whatIsSavedSurvivesANewRepository() = runTest {
        JsonFileObservationRepository(file, io = StandardTestDispatcher(testScheduler)).save(listOf(riego))
        assertEquals(listOf(riego), JsonFileObservationRepository(file).observations.first())
    }

    // Antes de escribir el archivo, quien escucha ya tiene la lista nueva.
    @Test
    fun memoryGoesFirst() = runTest {
        val repository = JsonFileObservationRepository(file, io = StandardTestDispatcher(testScheduler))
        repository.save(listOf(riego))
        assertEquals(listOf(riego), repository.observations.first())
    }

    // Un archivo roto no se pierde: se aparta con .bad y se empieza con las de partida.
    @Test
    fun aBrokenFileIsKeptAside() = runTest {
        file.writeText("{ esto no es una lista")
        assertEquals(emptyList(), JsonFileObservationRepository(file).observations.first())
        assertEquals("{ esto no es una lista", File(folder, "observations.json.bad").readText())
        assertTrue(!file.exists())
    }
}

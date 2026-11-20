package dev.bitacora.data

import app.cash.turbine.test
import dev.bitacora.data.testing.FakeRepository
import dev.bitacora.database.BitacoraDatabase
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.network.ObservationApi
import dev.bitacora.network.bitacoraClient
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

// Semana 34: lo que la app espera de cualquier ObservationRepository, escrito una vez y ejecutado contra los dos. Si el
// fake se porta distinto que Room, los tests de la app que lo usan prueban una app que no existe.
abstract class ObservationRepositoryContract {
    protected val a = Observation("A-1", Level.HIGH, "roya en el lote 2")
    protected val b = Observation("A-2", Level.LOW, "pulgón")
    protected val c = Observation("A-10", Level.MEDIUM, "riego del sector 3")

    protected abstract fun repository(initial: List<Observation> = emptyList()): ObservationRepository

    @Test
    fun itStartsWithTheInitialOnes() = runTest {
        assertEquals(listOf(a, b), repository(listOf(a, b)).observations.first())
    }

    @Test
    fun whatIsSavedIsRead() = runTest {
        val repository = repository()
        repository.save(listOf(a, b))
        assertEquals(listOf(a, b), repository.observations.first())
    }

    @Test
    fun whatLeavesTheListLeavesTheRepository() = runTest {
        val repository = repository(listOf(a, b, c))
        repository.save(listOf(a, c))
        assertEquals(listOf(a, c), repository.observations.first())
    }

    // A-10 va después de A-2: por el número, no por el texto, como nextCode. El fake de la semana 33 no lo hacía.
    @Test
    fun theOrderIsByNumber() = runTest {
        val repository = repository()
        repository.save(listOf(c, b, a))
        assertEquals(listOf("A-1", "A-2", "A-10"), repository.observations.first().map { it.code })
    }

    // Con Turbine: cada awaitItem espera la siguiente lista, y al salir del bloque no puede quedar ninguna sin mirar.
    @Test
    fun theFlowEmitsEachChange() = runTest {
        val repository = repository()
        repository.observations.test {
            assertEquals(emptyList(), awaitItem())
            repository.save(listOf(a))
            assertEquals(listOf(a), awaitItem())
            repository.save(listOf(a, b))
            assertEquals(listOf(a, b), awaitItem())
        }
    }
}

class FakeRepositoryTest : ObservationRepositoryContract() {
    override fun repository(initial: List<Observation>): ObservationRepository = FakeRepository(initial)
}

class RoomRepositoryTest : ObservationRepositoryContract() {
    @TempDir
    lateinit var dir: File

    private val opened = mutableListOf<BitacoraDatabase>()
    private val offline = ObservationApi(bitacoraClient(MockEngine { throw IOException("sin red") }, "https://api.bitacora.example/v1/"))

    override fun repository(initial: List<Observation>): ObservationRepository {
        val database = Room.databaseBuilder<BitacoraDatabase>(File(dir, "bitacora.db").path)
            .setDriver(BundledSQLiteDriver())
            .bitacora(File(dir, "observations.json"), initial)
            .build()
        opened += database
        return RoomObservationRepository(database, offline)
    }

    @AfterEach
    fun close() = opened.forEach { it.close() }
}

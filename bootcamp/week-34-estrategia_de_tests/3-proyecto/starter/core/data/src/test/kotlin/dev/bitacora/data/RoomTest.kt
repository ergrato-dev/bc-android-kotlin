package dev.bitacora.data

import dev.bitacora.database.BitacoraDatabase
import dev.bitacora.network.ObservationApi
import dev.bitacora.network.bitacoraClient

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.engine.mock.MockEngine
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.model.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 30: las observaciones en Room. En la JVM, con el SQLite empaquetado; en el móvil, el del sistema.
class RoomTest {
    @TempDir
    lateinit var dir: File

    private val a = Observation("A-1", Level.HIGH, "roya en el lote 2", Position(4.6, -74.08), "a-1.jpg")
    private val b = Observation("A-2", Level.LOW, "pulgón")
    private val c = Observation("A-10", Level.MEDIUM, "riego del sector 3")

    private val json get() = File(dir, "observations.json")

    // Semana 31: el repositorio también habla con el servidor. Aquí no se usa: uno sin red.
    private val offline = ObservationApi(bitacoraClient(MockEngine { throw IOException("sin red") }, "https://api.bitacora.example/v1/"))

    private fun open(initial: List<Observation> = emptyList()) =
        Room.databaseBuilder<BitacoraDatabase>(File(dir, "bitacora.db").path)
            .setDriver(BundledSQLiteDriver())
            .bitacora(json, initial)
            .build()

    // Abre, hace lo suyo y cierra: como la app, que no comparte la base de datos con la siguiente ejecución.
    private suspend fun <T> withRepository(initial: List<Observation> = emptyList(), block: suspend (ObservationRepository) -> T): T {
        val database = open(initial)
        try {
            return block(RoomObservationRepository(database, offline))
        } finally {
            database.close()
        }
    }

    @Test
    fun whatIsSavedSurvivesClosing() = runTest {
        withRepository { it.save(listOf(a, b)) }
        assertEquals(listOf(a, b), withRepository { it.observations.first() })
    }

    @Test
    fun whatLeavesTheListLeavesTheDatabase() = runTest {
        withRepository { it.save(listOf(a, b, c)) }
        withRepository { it.save(listOf(a, c)) }
        assertEquals(listOf(a, c), withRepository { it.observations.first() })
    }

    // A-10 va después de A-2: por el número, no por el texto, como nextCode.
    @Test
    fun theOrderIsByNumber() = runTest {
        withRepository { it.save(listOf(c, b, a)) }
        assertEquals(listOf("A-1", "A-2", "A-10"), withRepository { r -> r.observations.first().map { it.code } })
    }

    @Test
    fun theFlowEmitsEachChange() = runTest {
        withRepository { repository ->
            val emitted = Channel<List<String>>(Channel.UNLIMITED)
            backgroundScope.launch(Dispatchers.Default) {
                repository.observations.collect { list -> emitted.send(list.map { it.code }) }
            }
            assertEquals(emptyList(), emitted.receive())
            repository.save(listOf(a))
            assertEquals(listOf("A-1"), emitted.receive())
            repository.save(listOf(a, b))
            assertEquals(listOf("A-1", "A-2"), emitted.receive())
        }
    }

    @Test
    fun withoutJsonTheInitialOnes() = runTest {
        assertEquals(listOf(a, b), withRepository(initial = listOf(a, b)) { it.observations.first() })
    }

    // La semana 29 las guardaba en observations.json: la primera vez, pasan a la base de datos y el archivo se borra.
    @Test
    fun theJsonIsImportedAndDeleted() = runTest {
        json.writeText(encode(listOf(a, c)))
        assertEquals(listOf(a, c), withRepository(initial = listOf(b)) { it.observations.first() })
        assertFalse(json.exists())
    }

    @Test
    fun aBrokenJsonIsKeptAside() = runTest {
        json.writeText("{ esto no es una lista")
        assertEquals(listOf(b), withRepository(initial = listOf(b)) { it.observations.first() })
        assertTrue(File(dir, "observations.json.bad").exists())
    }

    // Las de partida, solo al crear la base de datos: si la persona las borra todas, no vuelven al abrir.
    @Test
    fun theInitialOnesOnlyOnce() = runTest {
        withRepository(initial = listOf(a, b)) { it.save(emptyList()) }
        assertEquals(emptyList(), withRepository(initial = listOf(a, b)) { it.observations.first() })
    }
}

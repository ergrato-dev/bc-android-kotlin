package dev.bitacora.data

import dev.bitacora.core.Observation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

// La fuente de verdad de las observaciones. La pantalla no sabe dónde viven: en un archivo, en una base de datos
// (semana 30) o, en los tests, en memoria.
interface ObservationRepository {
    val observations: Flow<List<Observation>>

    suspend fun save(observations: List<Observation>)
}

// Las observaciones en un archivo JSON. Primero la memoria y después el disco: quien escucha ve la lista nueva al
// momento, y el archivo se escribe fuera del hilo principal.
// ponytail: lee el archivo entero en el hilo que lo crea y lo reescribe entero en cada cambio; con cientos de
// observaciones, Room (semana 30).
class JsonFileObservationRepository(
    private val file: File,
    initial: List<Observation> = emptyList(),
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ObservationRepository {
    private val state = MutableStateFlow(read(initial))
    private val writing = Mutex()

    override val observations: Flow<List<Observation>> = state.asStateFlow()

    override suspend fun save(observations: List<Observation>) {
        state.value = observations
        withContext(io) {
            // En orden, y la última gana: sin el Mutex, dos escrituras a la vez se pisan el archivo temporal.
            writing.withLock {
                val temporary = File(file.path + ".tmp")
                temporary.writeText(encode(state.value))
                // Escribir aparte y mover: si el proceso muere a mitad, queda el archivo de antes, no medio archivo.
                Files.move(temporary.toPath(), file.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            }
        }
    }

    // Sin archivo, las de partida. Un archivo que no se puede leer se aparta, no se pisa: se puede recuperar a mano.
    private fun read(initial: List<Observation>): List<Observation> {
        if (!file.exists()) return initial
        return try {
            decode(file.readText())
        } catch (_: IllegalArgumentException) {
            // SerializationException (JSON roto) y Level.valueOf (un nivel que no existe) son IllegalArgumentException.
            file.renameTo(File(file.path + ".bad"))
            initial
        }
    }
}

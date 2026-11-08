package dev.bitacora.data

import dev.bitacora.core.Observation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

// La fuente de verdad de las observaciones. La pantalla no sabe dónde viven: en un archivo, en una base de datos
// (semana 30) o, en los tests, en memoria.
interface ObservationRepository {
    val observations: Flow<List<Observation>>

    suspend fun save(observations: List<Observation>)
}

// Las observaciones en un archivo JSON. Primero la memoria y después el disco: quien escucha ve la lista nueva al
// momento, y el archivo se escribe fuera del hilo principal.
// Paso 2: read lee el archivo (sin archivo, initial; uno que no se lee se aparta con .bad); save escribe en io, en un
// temporal que después se mueve encima (ATOMIC_MOVE), con un Mutex para que dos escrituras no se pisen.
@Suppress("unused")
class JsonFileObservationRepository(
    private val file: File,
    initial: List<Observation> = emptyList(),
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ObservationRepository {
    private val state = MutableStateFlow(initial)

    override val observations: Flow<List<Observation>> = state.asStateFlow()

    override suspend fun save(observations: List<Observation>) {
        state.value = observations
    }
}

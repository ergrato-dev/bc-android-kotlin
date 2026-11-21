package dev.bitacora.data

import dev.bitacora.model.Observation
import dev.bitacora.network.NetworkError
import kotlinx.coroutines.flow.Flow

// La fuente de verdad de las observaciones. La pantalla no sabe dónde viven: en una base de datos o, en los tests, en
// memoria. Ni de dónde llegan las del equipo: refresh las trae y las deja en el mismo sitio.
interface ObservationRepository {
    val observations: Flow<List<Observation>>

    suspend fun save(observations: List<Observation>)

    suspend fun refresh(): RefreshResult
}

sealed interface RefreshResult {
    data class Done(val received: Int) : RefreshResult
    data class Failed(val error: NetworkError) : RefreshResult
}

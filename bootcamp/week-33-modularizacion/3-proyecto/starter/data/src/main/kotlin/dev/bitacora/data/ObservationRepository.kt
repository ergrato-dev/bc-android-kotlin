package dev.bitacora.data

import dev.bitacora.core.Observation
import kotlinx.coroutines.flow.Flow

// La fuente de verdad de las observaciones. La pantalla no sabe dónde viven: en una base de datos o, en los tests, en
// memoria. Ni de dónde llegan las del equipo: refresh las trae y las deja en el mismo sitio.
interface ObservationRepository {
    val observations: Flow<List<Observation>>

    suspend fun save(observations: List<Observation>)

    suspend fun refresh(): RefreshResult
}

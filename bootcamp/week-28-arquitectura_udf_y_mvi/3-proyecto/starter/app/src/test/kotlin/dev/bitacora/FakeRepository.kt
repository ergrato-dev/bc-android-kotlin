package dev.bitacora

import dev.bitacora.core.Observation
import dev.bitacora.data.ObservationRepository
import kotlinx.coroutines.flow.MutableStateFlow

// El repositorio de los tests: en memoria, y cuenta las veces que se guarda.
class FakeRepository(initial: List<Observation> = emptyList()) : ObservationRepository {
    override val observations = MutableStateFlow(initial)
    var saves = 0
        private set

    override suspend fun save(observations: List<Observation>) {
        saves++
        this.observations.value = observations
    }
}

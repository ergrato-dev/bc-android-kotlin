package dev.bitacora

import dev.bitacora.core.Observation
import dev.bitacora.data.ListSettings
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.RefreshResult
import dev.bitacora.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

// El repositorio de los tests: en memoria, y cuenta las veces que se guarda. refresh contesta lo que diga el test
// (sin red); mientras answer no tiene valor, la petición espera.
class FakeRepository(initial: List<Observation> = emptyList()) : ObservationRepository {
    override val observations = MutableStateFlow(initial)
    var saves = 0
        private set
    var refreshes = 0
        private set
    val answer = MutableStateFlow<RefreshResult?>(RefreshResult.Done(0))

    override suspend fun save(observations: List<Observation>) {
        saves++
        this.observations.value = observations
    }

    override suspend fun refresh(): RefreshResult {
        refreshes++
        return answer.filterNotNull().first()
    }
}

// Las preferencias de los tests: en memoria, sin archivo.
class FakeSettings(initial: ListSettings = ListSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initial)

    override suspend fun save(settings: ListSettings) {
        this.settings.value = settings
    }
}

package dev.bitacora

import dev.bitacora.core.Observation
import dev.bitacora.data.ListSettings
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
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

// Las preferencias de los tests: en memoria, sin archivo.
class FakeSettings(initial: ListSettings = ListSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initial)

    override suspend fun save(settings: ListSettings) {
        this.settings.value = settings
    }
}

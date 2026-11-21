package dev.bitacora.data.testing

import dev.bitacora.data.ListSettings
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.RefreshResult
import dev.bitacora.data.SettingsRepository
import dev.bitacora.model.Observation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

// Semana 34: los fakes de la app, en los testFixtures de :core:data, junto a lo que imitan. Quien los necesita los
// pide con testImplementation(testFixtures(project(":core:data"))). El contrato (ObservationRepositoryContract, en los
// tests de este módulo) comprueba que este y RoomObservationRepository se portan igual.

// El repositorio de los tests: en memoria, y cuenta las veces que se guarda. refresh contesta lo que diga el test
// (sin red); mientras answer no tiene valor, la petición espera. Ordena por número, como la consulta de Room: el
// contrato lo encontró.
class FakeRepository(initial: List<Observation> = emptyList()) : ObservationRepository {
    override val observations = MutableStateFlow(initial.byNumber())
    var saves = 0
        private set
    var refreshes = 0
        private set
    val answer = MutableStateFlow<RefreshResult?>(RefreshResult.Done(0))

    override suspend fun save(observations: List<Observation>) {
        saves++
        this.observations.value = observations.byNumber()
    }

    override suspend fun refresh(): RefreshResult {
        refreshes++
        return answer.filterNotNull().first()
    }
}

private fun List<Observation>.byNumber() = sortedBy { it.code.substringAfter('-').toInt() }

// Las preferencias de los tests: en memoria, sin archivo.
class FakeSettings(initial: ListSettings = ListSettings()) : SettingsRepository {
    override val settings = MutableStateFlow(initial)

    override suspend fun save(settings: ListSettings) {
        this.settings.value = settings
    }
}

package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bitacora.core.Position
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
import dev.bitacora.data.decode
import dev.bitacora.data.encode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Una sola entrada (onIntent) y dos salidas: el estado, que siempre tiene un valor, y los efectos, que se entregan una
// vez. El ViewModel no decide nada: pasa cada intent por reduce, y hace lo que reduce no puede hacer (guardar, avisar).
// Las observaciones y las preferencias son de sus repositorios; el borrador, la ubicación y la borrada, del
// SavedStateHandle, para que sobrevivan a la muerte del proceso.
// Paso 4: que lo cree Hilt (@HiltViewModel, @Inject).
class BitacoraViewModel(
    private val handle: SavedStateHandle,
    private val repository: ObservationRepository,
    private val preferences: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        BitacoraUiState(
            draft = handle["draft"] ?: "",
            position = handle.get<DoubleArray>("position")?.let { Position(it[0], it[1]) },
            locating = handle["locating"] ?: Locating.IDLE,
            deleted = handle.get<String>("deleted")?.let { Deleted(decode(it).single(), handle["deletedAt"] ?: 0) },
        ),
    )
    val state: StateFlow<BitacoraUiState> = _state.asStateFlow()

    // Un Channel guarda el efecto hasta que alguien lo recoge (por ejemplo, tras girar el móvil), y lo entrega a uno
    // solo. Un SharedFlow sin replay lo perdería si en ese momento no hay nadie escuchando.
    private val effects = Channel<BitacoraEffect>(Channel.BUFFERED)
    val effect: Flow<BitacoraEffect> = effects.receiveAsFlow()

    init {
        // viewModelScope corre en Main.immediate: con un repositorio que ya tiene la lista, el primer valor llega aquí
        // mismo, antes del primer fotograma.
        viewModelScope.launch {
            repository.observations.collect { observations -> _state.update { it.copy(observations = observations) } }
        }
        // Paso 6: las preferencias, como las observaciones.
    }

    fun observation(code: String) = _state.value.observations.find { it.code == code }

    fun onIntent(intent: BitacoraIntent) {
        val before = _state.value
        val (after, effect) = reduce(before, intent)
        _state.value = after
        keep(after)
        if (after.observations !== before.observations) viewModelScope.launch { repository.save(after.observations) }
        // Paso 6: y guardarlas si cambian.
        effect?.let(effects::trySend)
    }

    private fun keep(state: BitacoraUiState) {
        handle["draft"] = state.draft
        handle["position"] = state.position?.let { doubleArrayOf(it.latitude, it.longitude) }
        handle["locating"] = state.locating
        handle["deleted"] = state.deleted?.let { encode(listOf(it.observation)) }
        handle["deletedAt"] = state.deleted?.at
    }
}

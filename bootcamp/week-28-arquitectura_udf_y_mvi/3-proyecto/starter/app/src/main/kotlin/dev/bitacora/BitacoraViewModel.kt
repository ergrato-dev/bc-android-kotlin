package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.bitacora.data.ObservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

// Una sola entrada (onIntent) y dos salidas: el estado y los efectos.
// Paso 4: el estado empieza con el borrador, la posición, locating y la borrada del handle; en init, las observaciones
// del repositorio entran en el estado. onIntent pasa el intent por reduce, guarda en el handle lo que no es la lista,
// llama a repository.save si la lista es otra (!==) y manda el efecto por un Channel(Channel.BUFFERED).
@Suppress("unused")
class BitacoraViewModel(private val handle: SavedStateHandle, private val repository: ObservationRepository) : ViewModel() {
    private val _state = MutableStateFlow(BitacoraUiState())
    val state: StateFlow<BitacoraUiState> = _state.asStateFlow()

    val effect: Flow<BitacoraEffect> = emptyFlow()

    fun observation(code: String) = _state.value.observations.find { it.code == code }

    @Suppress("UNUSED_PARAMETER")
    fun onIntent(intent: BitacoraIntent) {
    }
}

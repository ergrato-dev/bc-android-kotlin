package dev.bitacora

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

// Ya está hecho: lo que sale por messages. Los efectos del reducer, y el aviso de que no se pudo guardar.
sealed interface ChecklistMessage {
    data class Effect(val effect: ChecklistEffect) : ChecklistMessage
    data object SaveFailed : ChecklistMessage
}

// Una entrada, onIntent; dos salidas: state (siempre tiene valor) y messages (cada uno, una vez).
// Paso 1: en init, en viewModelScope, la lista del repositorio entra en el estado (collect y update).
class ChecklistViewModel(private val repository: ChecklistRepository) : ViewModel() {
    private val _state = MutableStateFlow(ChecklistState())
    val state: StateFlow<ChecklistState> = _state.asStateFlow()

    // Paso 3: un Channel(Channel.BUFFERED), y messages = channel.receiveAsFlow().
    val messages: Flow<ChecklistMessage> = emptyFlow()

    fun onIntent(intent: ChecklistIntent) {
        _state.value = reduce(_state.value, intent).state
        // Paso 2: si la lista ha cambiado (otra instancia: !==), repository.save en viewModelScope.
        // Paso 3: el efecto del reducer, por el Channel, con trySend.
        // Paso 4: si save lanza IOException, la lista vuelve a la del repositorio y sale SaveFailed.
    }
}

package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// El borrador vive en el SavedStateHandle, con las claves "text", "level" y "saved": sobrevive al giro (el ViewModel
// no se destruye) y a la muerte del proceso (el handle viaja en el Bundle).
class DraftViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // Paso 1: el estado, de los StateFlow del handle (getStateFlow), juntos con combine y convertidos en un StateFlow
    // con stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftUiState()).
    val state: StateFlow<DraftUiState> = MutableStateFlow(DraftUiState())

    // Paso 2: los cambios se escriben en el handle. El texto, cortado a MAX_LENGTH. Guardar, solo con texto y nivel:
    // añade la descripción, sin espacios a los lados, y vacía el borrador. Lee del handle, no de state.
    fun onTextChange(text: String) {
    }

    fun onLevelChange(level: Level) {
    }

    fun save() {
    }
}

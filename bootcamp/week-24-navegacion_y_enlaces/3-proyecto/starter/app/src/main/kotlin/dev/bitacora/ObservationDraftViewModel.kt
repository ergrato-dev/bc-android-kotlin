package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bitacora.core.Level
import dev.bitacora.core.MAX_DESCRIPTION
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.data.decode
import dev.bitacora.data.encode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// En qué está la ubicación mientras el borrador no tiene una.
enum class Locating { IDLE, LOCATING, DENIED }

// Todo lo que pinta la pantalla principal.
data class BitacoraUiState(
    val draft: String = "",
    val position: Position? = null,
    val locating: Locating = Locating.IDLE,
    val observations: List<Observation> = emptyList(),
)

// El borrador y las observaciones de la sesión viven en el SavedStateHandle: sobreviven al giro y a la muerte del
// proceso. Las observaciones se guardan como el JSON de data: un Bundle solo admite tipos sencillos. Antes de la
// primera, la sesión empieza con las de ejemplo de la variante (Samples).
// La pantalla recibe el estado como un StateFlow sacado del handle; las acciones leen del handle, que siempre está al
// día, y no de state, que va por detrás (teoría 05).
class ObservationDraftViewModel(private val handle: SavedStateHandle) : ViewModel() {
    val state: StateFlow<BitacoraUiState> = combine(
        handle.getStateFlow("draft", ""),
        handle.getStateFlow<DoubleArray?>("position", null),
        handle.getStateFlow("locating", Locating.IDLE),
        handle.getStateFlow<String?>("observations", null),
    ) { draft, position, locating, observations ->
        BitacoraUiState(draft, position?.toPosition(), locating, observations?.let(::decode) ?: Samples.observations)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BitacoraUiState(observations = Samples.observations))

    private val draft: String get() = handle["draft"] ?: ""

    // Un Bundle no guarda una Position, pero sí un DoubleArray.
    private var position: Position?
        get() = handle.get<DoubleArray>("position")?.toPosition()
        set(value) {
            handle["position"] = value?.let { doubleArrayOf(it.latitude, it.longitude) }
        }

    private val observations: List<Observation>
        get() = handle.get<String>("observations")?.let(::decode) ?: Samples.observations

    // Paso 5: la observación de un código, leída del handle, o null.
    fun onDraftChange(draft: String) {
        handle["draft"] = draft
    }

    fun locating() {
        handle["locating"] = Locating.LOCATING
    }

    fun denied() {
        handle["locating"] = Locating.DENIED
    }

    fun located(position: Position?) {
        this.position = position
        handle["locating"] = Locating.IDLE
    }

    fun add(level: Level): Observation? {
        if (draft.isBlank() || draft.length > MAX_DESCRIPTION) return null
        val observation = Observation("A-${observations.size + 1}", level, draft.trim(), position)
        handle["observations"] = encode(observations + observation)
        handle["draft"] = ""
        located(null)
        return observation
    }

    fun replace(observation: Observation) {
        handle["observations"] = encode(observations.map { if (it.code == observation.code) observation else it })
    }
}

private fun DoubleArray.toPosition() = Position(this[0], this[1])

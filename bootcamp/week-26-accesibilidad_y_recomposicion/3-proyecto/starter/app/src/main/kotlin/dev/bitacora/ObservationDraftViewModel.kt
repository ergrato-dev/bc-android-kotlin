package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bitacora.core.Level
import dev.bitacora.core.MAX_DESCRIPTION
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.core.nextCode
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
    // Paso 5: con cada tecla, combine vuelve a ejecutar la lambda y decode saca del JSON observaciones nuevas, iguales
    // a las de antes pero otras instancias: cada fila se recompone. Que decode vaya en un map del flujo de
    // "observations", antes de combine: solo se ejecuta cuando cambia el JSON.
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

    fun observation(code: String): Observation? = observations.find { it.code == code }

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
        val observation = Observation(observations.nextCode(), level, draft.trim(), position)
        handle["observations"] = encode(observations + observation)
        handle["draft"] = ""
        located(null)
        return observation
    }

    // Quita la observación y guarda en el handle cuál era y dónde estaba, para devolverla a su sitio.
    fun delete(code: String) {
        val index = observations.indexOfFirst { it.code == code }
        if (index < 0) return
        handle["deleted"] = encode(listOf(observations[index]))
        handle["deletedAt"] = index
        handle["observations"] = encode(observations.filterIndexed { i, _ -> i != index })
    }

    // Devuelve la última borrada a su sitio.
    fun undo() {
        val deleted = handle.get<String>("deleted")?.let(::decode)?.single() ?: return
        val at = handle.get<Int>("deletedAt") ?: return
        handle["observations"] = encode(observations.toMutableList().apply { add(at.coerceAtMost(size), deleted) })
        handle.remove<String>("deleted")
        handle.remove<Int>("deletedAt")
    }

    fun replace(observation: Observation) {
        handle["observations"] = encode(observations.map { if (it.code == observation.code) observation else it })
    }
}

private fun DoubleArray.toPosition() = Position(this[0], this[1])

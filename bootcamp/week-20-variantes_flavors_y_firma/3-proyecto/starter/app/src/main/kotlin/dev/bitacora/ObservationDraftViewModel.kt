package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.data.decode
import dev.bitacora.data.encode

// El borrador y las observaciones de la sesión viven en el SavedStateHandle: sobreviven al giro y a la muerte del
// proceso. Las observaciones se guardan como el JSON de data: un Bundle solo admite tipos sencillos.
class ObservationDraftViewModel(private val state: SavedStateHandle) : ViewModel() {
    var draft: String
        get() = state["draft"] ?: ""
        set(value) {
            state["draft"] = value
        }

    // La ubicación del borrador: un Bundle no guarda una Position, pero sí un DoubleArray.
    var position: Position?
        get() = state.get<DoubleArray>("position")?.let { Position(it[0], it[1]) }
        set(value) {
            state["position"] = value?.let { doubleArrayOf(it.latitude, it.longitude) }
        }

    val observations: List<Observation>
        // Paso 5: sin ninguna guardada, las de ejemplo de la variante (Samples.observations).
        get() = state.get<String>("observations")?.let(::decode) ?: emptyList()

    fun add(level: Level): Observation? {
        if (draft.isBlank()) return null
        val observation = Observation("A-${observations.size + 1}", level, draft.trim(), position)
        state["observations"] = encode(observations + observation)
        draft = ""
        position = null
        return observation
    }

    fun replace(observation: Observation) {
        state["observations"] = encode(observations.map { if (it.code == observation.code) observation else it })
    }
}

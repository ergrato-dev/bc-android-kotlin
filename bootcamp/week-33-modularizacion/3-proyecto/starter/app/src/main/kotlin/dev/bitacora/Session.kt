package dev.bitacora

import dev.bitacora.core.Level
import dev.bitacora.core.MAX_DESCRIPTION
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import dev.bitacora.core.nextCode
import dev.bitacora.data.ListSettings
import dev.bitacora.data.RefreshResult

// En qué está la ubicación mientras el borrador no tiene una.
enum class Locating { IDLE, LOCATING, DENIED }

// La última borrada y dónde estaba, para devolverla a su sitio.
data class Deleted(val observation: Observation, val at: Int)

// Todo lo que pinta la sesión, en un solo valor: la pantalla no mezcla trozos que van a destiempo.
data class BitacoraUiState(
    val draft: String = "",
    val position: Position? = null,
    val locating: Locating = Locating.IDLE,
    val observations: List<Observation> = emptyList(),
    val deleted: Deleted? = null,
    val settings: ListSettings = ListSettings(),
    // Hasta que llega la primera lista de la base de datos. Semana 30: con Room, la lista no está al crear el ViewModel.
    val loaded: Boolean = false,
    // Mientras se piden al servidor las del equipo. Semana 31.
    val refreshing: Boolean = false,
)

// Lo que enseña la lista: las observaciones, filtradas y ordenadas como dicen las preferencias. Se calcula, no se
// guarda: borrar y deshacer siguen trabajando sobre la lista entera.
fun BitacoraUiState.visible(): List<Observation> = observations
    .filter { !settings.onlyHigh || it.level == Level.HIGH }
    .let { if (settings.newestFirst) it.reversed() else it }

// Lo que puede pasar en la sesión: lo que hace la persona (escribir, añadir, borrar) y lo que llega de fuera (la
// ubicación). Sube de la pantalla al ViewModel; el estado baja.
sealed interface BitacoraIntent {
    data class EditDraft(val text: String) : BitacoraIntent
    data object StartLocating : BitacoraIntent
    data object LocationDenied : BitacoraIntent
    data class Located(val position: Position?) : BitacoraIntent
    data class Add(val level: Level) : BitacoraIntent
    data class Delete(val code: String) : BitacoraIntent
    data object Undo : BitacoraIntent
    data class Replace(val observation: Observation) : BitacoraIntent
    data object Share : BitacoraIntent
    data class ChangeSettings(val settings: ListSettings) : BitacoraIntent
    data object Refresh : BitacoraIntent
    data class Refreshed(val result: RefreshResult) : BitacoraIntent
}

// Lo que pasa una sola vez: si se guardase en el estado, se repetiría al girar el móvil.
sealed interface BitacoraEffect {
    data class ShowUndo(val code: String) : BitacoraEffect
    data class Share(val observations: List<Observation>) : BitacoraEffect
    data class ShowRefresh(val result: RefreshResult) : BitacoraEffect
}

data class Next(val state: BitacoraUiState, val effect: BitacoraEffect? = null)

// El reducer: el estado de antes y lo que ha pasado dan el de después. Sin Android, sin corrutinas y sin nada que
// guardar: se prueba con JUnit, en milisegundos. Lo que no cambia se devuelve tal cual (la misma instancia).
fun reduce(state: BitacoraUiState, intent: BitacoraIntent): Next = when (intent) {
    is BitacoraIntent.EditDraft -> Next(state.copy(draft = intent.text))
    BitacoraIntent.StartLocating -> Next(state.copy(locating = Locating.LOCATING))
    BitacoraIntent.LocationDenied -> Next(state.copy(locating = Locating.DENIED))
    is BitacoraIntent.Located -> Next(state.copy(position = intent.position, locating = Locating.IDLE))
    is BitacoraIntent.Add ->
        if (state.draft.isBlank() || state.draft.length > MAX_DESCRIPTION) Next(state)
        else Next(
            state.copy(
                draft = "",
                position = null,
                locating = Locating.IDLE,
                observations = state.observations + Observation(state.observations.nextCode(), intent.level, state.draft.trim(), state.position),
            ),
        )
    is BitacoraIntent.Delete -> {
        val at = state.observations.indexOfFirst { it.code == intent.code }
        if (at < 0) Next(state)
        else Next(
            state.copy(observations = state.observations.filterIndexed { i, _ -> i != at }, deleted = Deleted(state.observations[at], at)),
            BitacoraEffect.ShowUndo(intent.code),
        )
    }
    BitacoraIntent.Undo -> state.deleted?.let { (observation, at) ->
        Next(state.copy(observations = state.observations.toMutableList().apply { add(at.coerceAtMost(size), observation) }, deleted = null))
    } ?: Next(state)
    is BitacoraIntent.Replace ->
        Next(state.copy(observations = state.observations.map { if (it.code == intent.observation.code) intent.observation else it }))
    BitacoraIntent.Share -> Next(state, BitacoraEffect.Share(state.observations))
    is BitacoraIntent.ChangeSettings -> Next(state.copy(settings = intent.settings))
    // Una petición a la vez: un segundo toque mientras tanto no hace nada.
    BitacoraIntent.Refresh -> if (state.refreshing) Next(state) else Next(state.copy(refreshing = true))
    // La lista no cambia aquí: lo recibido entra en la base de datos y llega por el Flow, como todo lo demás.
    is BitacoraIntent.Refreshed -> Next(state.copy(refreshing = false), BitacoraEffect.ShowRefresh(intent.result))
}

package dev.bitacora

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position

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
)

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
}

// Lo que pasa una sola vez: si se guardase en el estado, se repetiría al girar el móvil.
sealed interface BitacoraEffect {
    data class ShowUndo(val code: String) : BitacoraEffect
    data class Share(val observations: List<Observation>) : BitacoraEffect
}

data class Next(val state: BitacoraUiState, val effect: BitacoraEffect? = null)

// El reducer: el estado de antes y lo que ha pasado dan el de después. Sin Android, sin corrutinas y sin nada que
// guardar. Lo que no cambia se devuelve tal cual (la misma instancia).
// Paso 3: un caso por intent, como hacían las funciones de ObservationDraftViewModel; Delete pide ShowUndo y Share pide
// Share. Con el borrador vacío o demasiado largo, Add no cambia nada.
@Suppress("UNUSED_PARAMETER")
fun reduce(state: BitacoraUiState, intent: BitacoraIntent): Next = Next(state)

package dev.bitacora

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Ya está hecho: cien observaciones de ejemplo, el borrador, añadir y cambiar el nivel.
data class Observation(val code: String, val level: Level, val description: String, val tags: List<String>)

class Session : ViewModel() {
    private val all = MutableStateFlow(
        List(100) { Observation("A-${it + 1}", Level.entries[it % 3], "plaga ${it + 1}", listOf("campo", "sector ${it % 5 + 1}")) },
    )
    val observations: StateFlow<List<Observation>> = all.asStateFlow()

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    fun onDraftChange(draft: String) {
        _draft.value = draft
    }

    fun add() {
        val description = _draft.value.trim().ifEmpty { return }
        all.update { it + Observation("A-${it.size + 1}", Level.LOW, description, listOf("campo")) }
        _draft.value = ""
    }

    fun nextLevel(code: String) = all.update { list -> list.map { if (it.code == code) it.copy(level = it.level.next) else it } }
}

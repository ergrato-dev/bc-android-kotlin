package dev.bitacora

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Ya está hecho: las observaciones de la sesión, en un ViewModel, con lo que se sigue y lo urgente.
data class Observation(
    val code: String,
    val level: Level,
    val description: String,
    val followed: Boolean = false,
    val urgent: Boolean = false,
)

class Session : ViewModel() {
    private val all = MutableStateFlow(
        listOf(
            Observation("A-1", Level.HIGH, "riego roto en el sector norte"),
            Observation("A-2", Level.MEDIUM, "hojas amarillas en la parcela 3"),
            Observation("A-3", Level.LOW, "pulgón en los tomates"),
        ),
    )
    val observations: StateFlow<List<Observation>> = all.asStateFlow()

    fun follow(code: String) = change(code) { it.copy(followed = !it.followed) }

    fun urgent(code: String) = change(code) { it.copy(urgent = !it.urgent) }

    private fun change(code: String, how: (Observation) -> Observation) =
        all.update { list -> list.map { if (it.code == code) how(it) else it } }
}

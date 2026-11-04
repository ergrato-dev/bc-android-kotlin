package dev.bitacora

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.SideEffect

// Ya está hecho, y solo para aprender: cuenta cuántas veces se compone cada cosa. SideEffect se ejecuta después de
// cada composición que termina, la primera incluida. Es @NonSkippableComposable porque su parámetro no cambia nunca:
// sin eso, Compose se la saltaría y no contaría nada. En una app de verdad no va: se mira con el Layout Inspector.
object Recompositions {
    val counts = mutableMapOf<String, Int>()

    @Composable
    @NonSkippableComposable
    fun count(name: String) {
        SideEffect { counts[name] = (counts[name] ?: 0) + 1 }
    }
}

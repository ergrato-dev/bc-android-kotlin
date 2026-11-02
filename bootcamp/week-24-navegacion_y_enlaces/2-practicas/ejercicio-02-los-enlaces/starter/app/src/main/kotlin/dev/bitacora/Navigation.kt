package dev.bitacora

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// Ya está hecho: el ejercicio 01, con el back stack que empieza por start.
@Serializable
data object SessionKey : NavKey

@Serializable
data class ObservationKey(val code: String) : NavKey

@Composable
fun BitacoraNav(session: Session, start: List<NavKey>) {
    val observations by session.observations.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(*start.toTypedArray())
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider {
            entry<SessionKey> {
                SessionScreen(observations, onOpen = { backStack += ObservationKey(it.code) })
            }
            entry<ObservationKey> { key ->
                val index = observations.indexOfFirst { it.code == key.code }
                val observation = observations[index]
                ObservationScreen(
                    observation,
                    onChoose = { level ->
                        session.replace(observation.copy(level = level))
                        backStack.removeLastOrNull()
                    },
                    onNext = { backStack += ObservationKey(observations[(index + 1) % observations.size].code) },
                )
            }
        },
    )
}

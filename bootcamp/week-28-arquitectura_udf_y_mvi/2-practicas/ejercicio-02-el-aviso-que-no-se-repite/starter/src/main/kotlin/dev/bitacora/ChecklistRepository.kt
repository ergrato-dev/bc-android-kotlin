package dev.bitacora

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Ya está hecho: dónde vive la lista. El ViewModel solo conoce la interfaz.
interface ChecklistRepository {
    val items: Flow<List<Item>>

    suspend fun save(items: List<Item>)
}

// En memoria. failing = true imita un disco lleno: save lanza y no guarda.
class InMemoryChecklistRepository(initial: List<Item> = emptyList()) : ChecklistRepository {
    override val items = MutableStateFlow(initial)
    var saves = 0
        private set
    var failing = false

    override suspend fun save(items: List<Item>) {
        if (failing) throw java.io.IOException("No space left on device")
        saves++
        this.items.value = items
    }
}

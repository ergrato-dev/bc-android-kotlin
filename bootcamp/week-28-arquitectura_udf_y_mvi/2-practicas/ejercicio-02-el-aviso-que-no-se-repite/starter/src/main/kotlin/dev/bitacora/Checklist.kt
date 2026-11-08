package dev.bitacora

// Ya está hecho: lo que hay que llevar a una salida al campo. El estado, los intents y los efectos.
data class Item(val name: String, val done: Boolean = false)

data class Removed(val item: Item, val at: Int)

data class ChecklistState(
    val draft: String = "",
    val items: List<Item> = emptyList(),
    val removed: Removed? = null,
)

sealed interface ChecklistIntent {
    data class EditDraft(val text: String) : ChecklistIntent
    data object Add : ChecklistIntent
    data class Toggle(val name: String) : ChecklistIntent
    data class Remove(val name: String) : ChecklistIntent
    data object Undo : ChecklistIntent
    data object ClearDone : ChecklistIntent
}

sealed interface ChecklistEffect {
    data class OfferUndo(val name: String) : ChecklistEffect
    data object AllPacked : ChecklistEffect
}

data class Next(val state: ChecklistState, val effect: ChecklistEffect? = null)

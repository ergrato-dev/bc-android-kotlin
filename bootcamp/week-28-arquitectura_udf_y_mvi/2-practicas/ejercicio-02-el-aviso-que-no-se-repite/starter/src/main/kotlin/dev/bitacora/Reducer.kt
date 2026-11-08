package dev.bitacora

// El reducer: sin corrutinas, sin Android, sin nada que guardar. Lo que no cambia se devuelve tal cual.
fun reduce(state: ChecklistState, intent: ChecklistIntent): Next = when (intent) {
    is ChecklistIntent.EditDraft -> Next(state.copy(draft = intent.text))
    ChecklistIntent.Add -> {
        val name = state.draft.trim()
        // Ni vacío ni repetido: el nombre es lo que identifica a cada cosa.
        if (name.isEmpty() || state.items.any { it.name.equals(name, ignoreCase = true) }) Next(state)
        else Next(state.copy(draft = "", items = state.items + Item(name)))
    }
    is ChecklistIntent.Toggle -> {
        if (state.items.none { it.name == intent.name }) Next(state)
        else {
            val items = state.items.map { if (it.name == intent.name) it.copy(done = !it.done) else it }
            // Solo al marcar la última: desmarcar y volver a marcar lo repite, pero girar el móvil no.
            val packed = items.all { it.done } && !state.items.all { it.done }
            Next(state.copy(items = items), if (packed) ChecklistEffect.AllPacked else null)
        }
    }
    is ChecklistIntent.Remove -> {
        val at = state.items.indexOfFirst { it.name == intent.name }
        if (at < 0) Next(state)
        else Next(
            state.copy(items = state.items.filterIndexed { i, _ -> i != at }, removed = Removed(state.items[at], at)),
            ChecklistEffect.OfferUndo(intent.name),
        )
    }
    ChecklistIntent.Undo -> state.removed?.let { (item, at) ->
        Next(state.copy(items = state.items.toMutableList().apply { add(at.coerceAtMost(size), item) }, removed = null))
    } ?: Next(state)
    ChecklistIntent.ClearDone ->
        if (state.items.none { it.done }) Next(state) else Next(state.copy(items = state.items.filterNot { it.done }))
}

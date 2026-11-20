package dev.bitacora.notes

// El resumen de la pantalla de notas.
suspend fun summary(store: NoteStore): String {
    val notes = store.notes()
    return when (notes.size) {
        0 -> "Sin notas"
        1 -> "1 nota: ${notes.single()}"
        else -> "${notes.size} notas: ${notes.joinToString(", ")}"
    }
}

// Lo que escribe la persona: una nota por línea. Devuelve cuántas eran nuevas.
suspend fun import(store: NoteStore, text: String): Int = text.lines().count { store.add(it) }

package dev.bitacora.notes

// El almacén de los tests del resumen: en memoria, sin archivo.
class FakeNoteStore(initial: List<String> = emptyList()) : NoteStore {
    private val notes = initial.toMutableList()

    override suspend fun notes(): List<String> = notes.toList()

    override suspend fun add(note: String): Boolean {
        notes += note
        return true
    }

    override suspend fun remove(note: String) {
        notes -= note
    }
}

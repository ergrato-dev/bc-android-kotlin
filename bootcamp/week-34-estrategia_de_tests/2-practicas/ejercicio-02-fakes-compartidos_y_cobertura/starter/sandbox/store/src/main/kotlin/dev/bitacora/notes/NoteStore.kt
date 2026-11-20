package dev.bitacora.notes

import java.io.File

// Las notas sueltas de la Bitácora: textos cortos, sin repetir, en orden alfabético.
interface NoteStore {
    suspend fun notes(): List<String>

    // Guarda la nota sin espacios alrededor. Una vacía, o una que ya está, no se guarda: devuelve false.
    suspend fun add(note: String): Boolean

    suspend fun remove(note: String)
}

// La de verdad: una nota por línea en un archivo. Si el archivo no existe, no hay notas.
class FileNoteStore(private val file: File) : NoteStore {
    override suspend fun notes(): List<String> = if (file.exists()) file.readLines().filter { it.isNotEmpty() }.sorted() else emptyList()

    override suspend fun add(note: String): Boolean {
        val clean = note.trim()
        val notes = notes()
        if (clean.isEmpty() || clean in notes) return false
        write(notes + clean)
        return true
    }

    override suspend fun remove(note: String) = write(notes() - note)

    private fun write(notes: List<String>) {
        file.writeText(notes.sorted().joinToString("\n"))
    }
}

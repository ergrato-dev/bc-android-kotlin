package dev.bitacora.notes

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Lo que solo tiene el archivo: que las notas siguen ahí con otro FileNoteStore.
class FileNoteStoreTest {
    @TempDir
    lateinit var dir: File

    @Test
    fun theNotesSurviveInTheFile() = runTest {
        FileNoteStore(File(dir, "notes.txt")).add("riego")
        assertEquals(listOf("riego"), FileNoteStore(File(dir, "notes.txt")).notes())
    }
}

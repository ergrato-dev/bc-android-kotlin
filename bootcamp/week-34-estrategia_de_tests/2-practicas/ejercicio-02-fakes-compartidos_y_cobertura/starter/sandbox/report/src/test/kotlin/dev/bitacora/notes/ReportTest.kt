package dev.bitacora.notes

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportTest {
    @Test
    fun noNotes() = runTest {
        assertEquals("Sin notas", summary(FakeNoteStore()))
    }

    @Test
    fun oneNote() = runTest {
        assertEquals("1 nota: riego", summary(FakeNoteStore(listOf("riego"))))
    }

    @Test
    fun severalNotes() = runTest {
        assertEquals("2 notas: poda, riego", summary(FakeNoteStore(listOf("poda", "riego"))))
    }

    @Test
    fun importCountsTheNewOnes() = runTest {
        assertEquals(2, import(FakeNoteStore(), "poda\nriego"))
    }
}

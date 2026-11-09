package dev.bitacora

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GraphTest {
    @TempDir
    lateinit var dir: File

    private fun component(at: File = dir) = DaggerTripComponent.factory().create(at)

    // Paso 1: quien pide ChecklistRepository recibe la del archivo.
    @Test
    fun theRepositoryIsTheFileOne() {
        assertIs<FileChecklistRepository>(component().repository())
    }

    @Test
    fun itWritesInTheGivenDir() {
        component().planner().pack("agua")
        assertEquals("agua\n", File(dir, "checklist.txt").readText())
    }

    // Paso 2: una sola para todo el componente.
    @Test
    fun oneRepositoryForTheWholeComponent() {
        val trip = component()
        assertSame(trip.repository(), trip.repository())
    }

    @Test
    fun twoPlannersShareIt() {
        val trip = component()
        assertSame(trip.planner().repository, trip.planner().repository)
    }

    // Lo que no tiene scope se crea cada vez que se pide.
    @Test
    fun aPlannerIsNewEachTime() {
        val trip = component()
        assertNotSame(trip.planner(), trip.planner())
    }

    // @Singleton quiere decir "una por componente", no "una en todo el programa".
    @Test
    fun twoComponentsTwoRepositories(@TempDir other: File) {
        assertNotSame(component().repository(), component(other).repository())
    }
}

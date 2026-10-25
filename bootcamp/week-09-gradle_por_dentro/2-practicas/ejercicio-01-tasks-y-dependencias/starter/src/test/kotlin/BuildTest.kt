import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome

class BuildTest {
    @Test
    fun helloHasAGroupAndADescription() {
        val sb = Sandbox()
        assertTrue(sb.run("hello").output.contains("Hola desde Gradle"))
        val help = sb.run("help", "--task", "hello").output
        assertTrue(Regex("Group\\s+bitacora").containsMatchIn(help), help)
        assertTrue(Regex("Description\\s+\\S").containsMatchIn(help), help)
    }

    @Test
    fun stageCopiesOnlyTheCsvFilesRenamed() {
        val sb = Sandbox()
        sb.run("stage")
        assertEquals(listOf("obs-a.csv", "obs-b.csv"), sb.file("build/stage").list()!!.sorted())
        assertEquals(sb.file("data/a.csv").readText(), sb.file("build/stage/obs-a.csv").readText())
    }

    @Test
    fun bundleIsAZipNamedAfterTheVersionAndRunsStageFirst() {
        val sb = Sandbox()
        val result = sb.run("bundle")
        assertEquals(listOf("bitacora-1.0.0.zip"), sb.file("build/dist").list()!!.toList())
        val entries = ZipFile(sb.file("build/dist/bitacora-1.0.0.zip")).use { zip -> zip.entries().toList().map { it.name } }
        assertEquals(listOf("obs-a.csv", "obs-b.csv"), entries.sorted())
        assertTrue(result.executed().indexOf(":stage") < result.executed().indexOf(":bundle"))
    }

    @Test
    fun theSecondRunIsUpToDateUntilAnInputChanges() {
        val sb = Sandbox()
        val first = sb.run("bundle")
        assertEquals(TaskOutcome.SUCCESS, first.outcome(":stage"))
        assertEquals(TaskOutcome.SUCCESS, first.outcome(":bundle"))

        val second = sb.run("bundle")
        assertEquals(TaskOutcome.UP_TO_DATE, second.outcome(":stage"))
        assertEquals(TaskOutcome.UP_TO_DATE, second.outcome(":bundle"))

        sb.file("data/notes.txt").appendText("otra nota\n")           // no es un .csv: no es una entrada
        assertEquals(TaskOutcome.UP_TO_DATE, sb.run("bundle").outcome(":stage"))

        sb.file("data/a.csv").appendText("A-3,9.0\n")                // sí lo es
        val third = sb.run("bundle")
        assertEquals(TaskOutcome.SUCCESS, third.outcome(":stage"))
        assertEquals(TaskOutcome.SUCCESS, third.outcome(":bundle"))
    }

    @Test
    fun announceAlwaysRunsRightAfterBundle() {
        val sb = Sandbox()
        val result = sb.run("bundle")
        assertEquals(listOf(":stage", ":bundle", ":announce"), result.executed())
        assertTrue(result.output.contains("Paquete listo: bitacora-1.0.0.zip"), result.output)
    }

    @Test
    fun quietSkipsAnnounce() {
        val sb = Sandbox()
        val result = sb.run("bundle", "-Pquiet")
        assertEquals(TaskOutcome.SKIPPED, result.outcome(":announce"))
        assertFalse(result.output.contains("Paquete listo"))
    }

    @Test
    fun releaseBuildsTheBundleAndResetCleansUp() {
        val sb = Sandbox()
        val result = sb.run("release")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":bundle"))
        assertTrue(sb.file("build/dist/bitacora-1.0.0.zip").exists())
        sb.run("reset")
        assertFalse(sb.file("build").exists())
        assertNull(sb.run("hello").outcome(":stage"))                // hello no depende de nada
    }
}

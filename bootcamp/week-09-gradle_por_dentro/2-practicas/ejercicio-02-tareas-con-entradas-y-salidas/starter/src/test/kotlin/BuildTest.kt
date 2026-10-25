import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    @Test
    fun countsTheNonBlankLinesWithoutTheHeader() {
        val sb = Sandbox()
        val result = sb.run("summary")
        assertEquals("lines=3\n", sb.file("build/reports/lines.txt").readText())
        assertTrue(result.output.contains("Resumen: lines=3"), result.output)
        assertEquals(listOf(":countObservations", ":summary"), result.executed())
    }

    @Test
    fun unchangedInputsAreUpToDate() {
        val sb = Sandbox()
        assertEquals(TaskOutcome.SUCCESS, sb.run("countObservations").outcome(":countObservations"))
        assertEquals(TaskOutcome.UP_TO_DATE, sb.run("countObservations").outcome(":countObservations"))
    }

    @Test
    fun changingTheSourceRunsItAgain() {
        val sb = Sandbox()
        sb.run("countObservations")
        sb.file("data/observations.csv").appendText("C-1,1.0\n")
        val result = sb.run("countObservations")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":countObservations"))
        assertEquals("lines=4\n", sb.file("build/reports/lines.txt").readText())
    }

    @Test
    fun deletingTheOutputRunsItAgain() {
        val sb = Sandbox()
        sb.run("countObservations")
        sb.file("build/reports/lines.txt").delete()
        assertEquals(TaskOutcome.SUCCESS, sb.run("countObservations").outcome(":countObservations"))
        assertTrue(sb.file("build/reports/lines.txt").exists())
    }

    @Test
    fun theHeaderFlagIsAnInput() {
        val sb = Sandbox()
        sb.run("countObservations")
        val withHeader = sb.run("countObservations", "-PskipHeader=false")
        assertEquals(TaskOutcome.SUCCESS, withHeader.outcome(":countObservations"))
        assertEquals("lines=4\n", sb.file("build/reports/lines.txt").readText())
        assertEquals(TaskOutcome.UP_TO_DATE, sb.run("countObservations", "-PskipHeader=false").outcome(":countObservations"))
        assertEquals(TaskOutcome.SUCCESS, sb.run("countObservations").outcome(":countObservations"))
        assertEquals("lines=3\n", sb.file("build/reports/lines.txt").readText())
    }

    @Test
    fun theResultIsReusedFromTheBuildCache() {
        val sb = Sandbox()
        assertEquals(TaskOutcome.SUCCESS, sb.run("countObservations", "--build-cache").outcome(":countObservations"))
        sb.run("clean")
        val again = sb.run("countObservations", "--build-cache")
        assertEquals(TaskOutcome.FROM_CACHE, again.outcome(":countObservations"))
        assertEquals("lines=3\n", sb.file("build/reports/lines.txt").readText())
    }

    @Test
    fun aMissingSourceFailsWithAClearMessage() {
        val sb = Sandbox()
        sb.file("data/observations.csv").delete()
        val result = sb.fail("countObservations")
        assertTrue(result.output.contains("property 'source'"), result.output)
        assertTrue(result.output.contains("doesn't exist"), result.output)
    }
}

import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    private val count = ":countObservations"

    @Test
    fun cachingIsOnWithoutTheFlag() {
        val sb = Sandbox()
        sb.run("compileKotlin")
        sb.run("clean")
        assertEquals(TaskOutcome.FROM_CACHE, sb.run("compileKotlin").outcome(":compileKotlin"))
    }

    @Test
    fun theCustomTaskComesFromTheCacheAfterClean() {
        val sb = Sandbox()
        sb.run(count)
        sb.run("clean")
        val result = sb.run(count)
        assertEquals(TaskOutcome.FROM_CACHE, result.outcome(count), result.output)
        assertEquals("observations=3\n", sb.file("build/reports/observations.txt").readText())
    }

    @Test
    fun aRelocatedCopyReusesTheResults() {
        val first = Sandbox()
        first.run(count, "compileKotlin")
        val result = first.relocated().run(count, "compileKotlin")
        assertEquals(TaskOutcome.FROM_CACHE, result.outcome(count), result.output)
        assertEquals(TaskOutcome.FROM_CACHE, result.outcome(":compileKotlin"), result.output)
    }

    @Test
    fun changingTheCsvMissesTheCache() {
        val sb = Sandbox()
        sb.run(count)
        sb.run("clean")
        sb.file("data/observations.csv").appendText("A-4,HIGH,helada\n")
        assertEquals(TaskOutcome.SUCCESS, sb.run(count).outcome(count))
        assertEquals("observations=4\n", sb.file("build/reports/observations.txt").readText())
    }

    @Test
    fun goingBackToAnOldInputHitsTheCacheAgain() {
        val sb = Sandbox()
        sb.run(count)
        assertEquals(TaskOutcome.SUCCESS, sb.run(count, "-PskipHeader=false").outcome(count))
        assertEquals("observations=4\n", sb.file("build/reports/observations.txt").readText())
        val back = sb.run(count)
        assertEquals(TaskOutcome.FROM_CACHE, back.outcome(count), back.output)
        assertEquals("observations=3\n", sb.file("build/reports/observations.txt").readText())
    }

    @Test
    fun theTaskDeclaresARelativePath() {
        val text = Sandbox().file("build.gradle.kts").readText()
        assertTrue(text.contains("@CacheableTask"), "CountObservations no es cacheable")
        assertTrue(text.contains("PathSensitivity.RELATIVE"), "source necesita @PathSensitive(PathSensitivity.RELATIVE)")
    }

    @Test
    fun theProgramStillRuns() {
        assertEquals("Bitácora", Sandbox().run("run", "-q").output.trim())
    }
}

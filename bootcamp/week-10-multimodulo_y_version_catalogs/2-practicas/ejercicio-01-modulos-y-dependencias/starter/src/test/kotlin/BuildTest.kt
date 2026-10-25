import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    @Test
    fun theThreeModulesAreListed() {
        val out = Sandbox().run("projects").output
        listOf(":app", ":core", ":data").forEach { assertTrue(out.contains("Project '$it'"), out) }
    }

    @Test
    fun theAppRunsAndSeesTheTypesOfCore() {
        val out = Sandbox().run(":app:run", "-q").output
        assertEquals(
            """[{"code":"A-1","level":"HIGH","description":"riego"}]""" + "\n" + "{LOW=0, MEDIUM=0, HIGH=1}",
            out.trim(),
        )
    }

    @Test
    fun testingDataBuildsCoreButNotTheApp() {
        val result = Sandbox().run(":data:test")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":data:test"))
        assertTrue(":core:compileKotlin" in result.executed(), result.output)
        assertFalse(result.executed().any { it.startsWith(":app:") }, result.output)
    }

    @Test
    fun theModulesAreBuiltInDependencyOrder() {
        val order = Sandbox().run(":app:compileKotlin").executed()
        assertTrue(order.indexOf(":core:compileKotlin") < order.indexOf(":data:compileKotlin"), order.toString())
        assertTrue(order.indexOf(":data:compileKotlin") < order.indexOf(":app:compileKotlin"), order.toString())
    }

    @Test
    fun everyModuleRunsItsOwnTests() {
        val result = Sandbox().run("test")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":core:test"))
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":data:test"))
        assertEquals(TaskOutcome.NO_SOURCE, result.outcome(":app:test"))
    }

    @Test
    fun changingABodyInCoreDoesNotRecompileTheOthers() {
        val sb = Sandbox()
        sb.run(":app:compileKotlin")
        sb.file("core/src/main/kotlin/Observation.kt").let {
            it.writeText(it.readText().replace("count { it.level == l }", "this.count { it.level == l }"))
        }
        val result = sb.run(":app:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":core:compileKotlin"))
        assertEquals(TaskOutcome.UP_TO_DATE, result.outcome(":data:compileKotlin"))
        assertEquals(TaskOutcome.UP_TO_DATE, result.outcome(":app:compileKotlin"))
    }

    @Test
    fun addingAPublicFunctionInCoreRecompilesTheOthers() {
        val sb = Sandbox()
        sb.run(":app:compileKotlin")
        sb.file("core/src/main/kotlin/Observation.kt").appendText("\nfun List<Observation>.codes(): List<String> = map { it.code }\n")
        val result = sb.run(":app:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":data:compileKotlin"))
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":app:compileKotlin"))
    }
}

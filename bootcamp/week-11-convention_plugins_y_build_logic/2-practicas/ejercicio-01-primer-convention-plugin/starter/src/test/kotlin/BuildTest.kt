import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val modules = listOf("core", "data", "app")

    @Test
    fun theWholeBuildPassesAndTheAppRuns() {
        val sb = Sandbox()
        sb.run("build")
        assertEquals(
            """[{"code":"A-1","level":"HIGH","description":"riego"}]""" + "\n" + "{LOW=0, MEDIUM=0, HIGH=1}",
            sb.run(":app:run", "-q").output.trim(),
        )
    }

    @Test
    fun theConventionPluginIsBuiltByBuildLogic() {
        val result = Sandbox().run(":core:compileKotlin")
        assertTrue(":build-logic:compileKotlin" in result.executed(), result.output)
    }

    @Test
    fun everyModuleAppliesTheConventionAndRepeatsNothing() {
        val sb = Sandbox()
        modules.forEach { m ->
            val text = sb.file("$m/build.gradle.kts").readText()
            assertTrue(text.contains("""id("bitacora.kotlin-base")"""), "$m no aplica bitacora.kotlin-base")
            listOf("jvmToolchain", "allWarningsAsErrors", "useJUnitPlatform", "plugins.kotlin.jvm").forEach {
                assertFalse(text.contains(it), "$m todavía repite $it")
            }
        }
    }

    @Test
    fun theClassesAreCompiledForJava21() {
        val sb = Sandbox()
        sb.run(":app:compileKotlin")
        listOf("core/build/classes/kotlin/main/Level.class", "app/build/classes/kotlin/main/MainKt.class").forEach { path ->
            val bytes = sb.file(path).readBytes()
            // Bytes 6 y 7 de un .class: la versión del formato. 65 es Java 21.
            assertEquals(65, (bytes[6].toInt() shl 8) or bytes[7].toInt(), path)
        }
    }

    @Test
    fun aWarningInAnyModuleBreaksTheBuild() {
        val sb = Sandbox()
        sb.file("app/src/main/kotlin/Old.kt").writeText("@Deprecated(\"old\")\nfun old() = 1\n\nfun useOld() = old()\n")
        val result = sb.fail(":app:compileKotlin")
        assertTrue(result.output.contains("warnings found and -Werror specified"), result.output)
    }

    @Test
    fun theTestsOfCoreAndDataRun() {
        val sb = Sandbox()
        val result = sb.run("test")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":core:test"))
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":data:test"))
        assertTrue(sb.file("core/build/test-results/test/TEST-CoreTest.xml").exists())
        assertTrue(sb.file("data/build/test-results/test/TEST-DataTest.xml").exists())
    }

    @Test
    fun changingTheConventionDoesNotRecompileTheModules() {
        val sb = Sandbox()
        sb.run("build")
        sb.file("build-logic/src/main/kotlin/bitacora.kotlin-base.gradle.kts").appendText("\ndescription = \"Bitácora\"\n")
        val result = sb.run("build")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":build-logic:jar"), result.output)
        modules.forEach { assertEquals(TaskOutcome.UP_TO_DATE, result.outcome(":$it:compileKotlin"), result.output) }
    }
}

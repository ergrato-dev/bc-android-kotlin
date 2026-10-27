import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val conventions = listOf("bitacora.kotlin-library", "bitacora.kotlin-application")
        .map { "build-logic/src/main/kotlin/$it.gradle.kts" }

    private fun code(text: String) = text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("//") }

    @Test
    fun theWholeBuildPassesAndTheAppRuns() {
        val sb = Sandbox()
        sb.run("build")
        assertTrue(sb.run(":app:run", "-q").output.contains("{LOW=0, MEDIUM=0, HIGH=1}"))
    }

    @Test
    fun coreIsOnlyItsConvention() {
        val core = code(Sandbox().file("core/build.gradle.kts").readText())
        assertEquals(listOf("plugins {", """id("bitacora.kotlin-library")""", "}"), core)
    }

    @Test
    fun theLibraryConventionBringsTheTestLibraries() {
        val sb = Sandbox()
        listOf("core", "data").forEach { m ->
            assertFalse(sb.file("$m/build.gradle.kts").readText().contains("testImplementation"), "$m todavía declara sus dependencias de prueba")
        }
        val out = sb.run(":data:dependencies", "--configuration", "testRuntimeClasspath").output
        listOf("org.junit:junit-bom:6.1.3", "org.jetbrains.kotlin:kotlin-test", "org.junit.platform:junit-platform-launcher").forEach {
            assertTrue(out.contains(it), out)
        }
    }

    @Test
    fun theConventionsComposeTheBase() {
        val sb = Sandbox()
        conventions.forEach { path ->
            val text = sb.file(path).readText()
            assertTrue(code(text).contains("""id("bitacora.kotlin-base")"""), "$path no aplica bitacora.kotlin-base")
            assertFalse(text.contains("jvmToolchain"), "$path repite lo que ya hace la base")
        }
    }

    @Test
    fun theVersionsLiveOnlyInTheCatalog() {
        val sb = Sandbox()
        (listOf("build-logic/build.gradle.kts", "build-logic/src/main/kotlin/bitacora.kotlin-base.gradle.kts") + conventions).forEach { path ->
            assertFalse(Regex("\\d+\\.\\d+\\.\\d+").containsMatchIn(code(sb.file(path).readText()).joinToString("\n")), "$path tiene una versión escrita a mano")
        }
        val toml = sb.file("gradle/libs.versions.toml").readText()
        listOf("2.4.21", "1.12.0", "6.1.3").forEach { assertEquals(1, Regex(Regex.escape(it)).findAll(toml).count(), it) }
    }

    @Test
    fun onlyTheAppIsAProgramAndItHasNoTestLibraries() {
        val sb = Sandbox()
        assertTrue(sb.fail(":core:run").output.contains("Cannot locate tasks that match ':core:run'"))
        val out = sb.run(":app:dependencies", "--configuration", "testRuntimeClasspath").output
        assertFalse(out.contains("junit-bom"), out)
    }

    @Test
    fun theKotlinPluginIsLoadedOnlyOnce() {
        val out = Sandbox().run("build").output
        assertFalse(out.contains("loaded multiple times"), out)
    }
}

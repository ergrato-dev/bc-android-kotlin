import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val buildFiles = listOf("build.gradle.kts", "core/build.gradle.kts", "data/build.gradle.kts", "app/build.gradle.kts")

    @Test
    fun theWholeBuildPassesAndTheAppRuns() {
        val sb = Sandbox()
        sb.run("build")
        assertTrue(sb.run(":app:run", "-q").output.contains("{LOW=0, MEDIUM=0, HIGH=1}"))
    }

    @Test
    fun eachVersionIsWrittenOnceInTheCatalog() {
        val toml = Sandbox().file("gradle/libs.versions.toml").readText()
        listOf("2.4.21", "1.12.0", "6.1.3").forEach { version ->
            assertEquals(1, Regex(Regex.escape(version)).findAll(toml).count(), "$version tiene que aparecer una vez en libs.versions.toml")
        }
    }

    @Test
    fun noBuildFileHasALiteralVersion() {
        val sb = Sandbox()
        buildFiles.forEach { path ->
            val text = sb.file(path).readText().lines().filterNot { it.trimStart().startsWith("//") }.joinToString("\n")
            assertFalse(Regex("\\d+\\.\\d+\\.\\d+").containsMatchIn(text), "$path todavía tiene una versión escrita a mano")
        }
    }

    @Test
    fun theCatalogVersionsReachTheClasspaths() {
        val sb = Sandbox()
        val data = sb.run(":data:dependencies", "--configuration", "runtimeClasspath").output
        assertTrue(data.contains("org.jetbrains.kotlinx:kotlinx-serialization-json -> 1.12.0"), data)
        val test = sb.run(":core:dependencies", "--configuration", "testRuntimeClasspath").output
        assertTrue(test.contains("org.junit:junit-bom:6.1.3"), test)
    }

    @Test
    fun theKotlinPluginIsLoadedOnlyOnce() {
        val out = Sandbox().run("build").output
        assertFalse(out.contains("loaded multiple times"), out)
    }

    @Test
    fun aModuleCannotDeclareItsOwnRepositories() {
        val sb = Sandbox()
        sb.file("core/build.gradle.kts").appendText("\nrepositories {\n    mavenCentral()\n}\n")
        val result = sb.fail(":core:compileKotlin")
        assertTrue(result.output.contains("prefer settings repositories over project repositories"), result.output)
    }

    @Test
    fun theModulesUseTheTestingBundle() {
        val sb = Sandbox()
        assertTrue(sb.file("core/build.gradle.kts").readText().contains("libs.bundles.testing"))
        assertTrue(sb.file("data/build.gradle.kts").readText().contains("libs.bundles.testing"))
    }
}

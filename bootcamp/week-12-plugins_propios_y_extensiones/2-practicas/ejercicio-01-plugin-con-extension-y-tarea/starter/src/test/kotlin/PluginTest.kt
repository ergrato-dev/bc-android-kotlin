import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PluginTest {
    private val agro = """
        plugins {
            id("bitacora.build-info")
        }

        version = "1.2.0"

        buildInfo {
            domain = "agro"
        }
    """

    // Pruebas unitarias: ProjectBuilder crea un proyecto en memoria, sin ejecutar Gradle. Rápidas.

    @Test
    fun theExtensionAndTheTaskExist() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("bitacora.build-info")
        assertNotNull(project.extensions.findByName("buildInfo"))
        val task = project.tasks.getByName("generateBuildInfo")
        assertEquals("bitacora", task.group)
        assertTrue(!task.description.isNullOrBlank(), "generateBuildInfo necesita una descripción")
    }

    @Test
    fun theVersionDefaultsToTheProjectVersion() {
        val project = ProjectBuilder.builder().build()
        project.version = "2.0.0"
        project.pluginManager.apply("bitacora.build-info")
        assertEquals("2.0.0", project.extensions.getByType(BuildInfoExtension::class.java).version.get())
    }

    // Pruebas funcionales: TestKit ejecuta Gradle de verdad sobre un build de prueba. Lentas, pero lo prueban todo.

    @Test
    fun theFileHasTheVersionAndTheDomain() {
        val build = TestBuild(agro)
        build.run("generateBuildInfo")
        assertEquals("version=1.2.0\ndomain=agro\n", build.file("build/generated/build-info/build-info.properties").readText())
    }

    @Test
    fun withJavaTheFileEndsUpInTheResources() {
        val build = TestBuild(agro.replace("plugins {", "plugins {\n    java"))
        val result = build.run("processResources")
        assertEquals(TaskOutcome.SUCCESS, result.outcome(":generateBuildInfo"), result.output)
        assertEquals("version=1.2.0\ndomain=agro\n", build.file("build/resources/main/build-info.properties").readText())
    }

    @Test
    fun theTaskIsUpToDateUntilTheDomainChanges() {
        val build = TestBuild(agro)
        build.run("generateBuildInfo")
        assertEquals(TaskOutcome.UP_TO_DATE, build.run("generateBuildInfo").outcome(":generateBuildInfo"))
        build.file("build.gradle.kts").writeText(build.file("build.gradle.kts").readText().replace("agro", "obra"))
        assertEquals(TaskOutcome.SUCCESS, build.run("generateBuildInfo").outcome(":generateBuildInfo"))
    }

    @Test
    fun aMissingDomainFailsBeforeRunningTheTask() {
        val build = TestBuild(agro.replace("domain = \"agro\"", ""))
        val result = build.fail("generateBuildInfo")
        assertTrue(result.output.contains("property 'domain' doesn't have a configured value"), result.output)
    }

    @Test
    fun theConfigurationCacheIsReused() {
        val build = TestBuild(agro)
        build.run("generateBuildInfo", "--configuration-cache")
        val result = build.run("generateBuildInfo", "--configuration-cache")
        assertTrue(result.output.contains("Configuration cache entry reused"), result.output)
    }
}

import org.gradle.testkit.runner.TaskOutcome
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublishTest {
    private val marker = "build/repo/bitacora/build-info/bitacora.build-info.gradle.plugin/1.0.0/bitacora.build-info.gradle.plugin-1.0.0.pom"
    private val jar = "build/repo/dev/bitacora/build-info-plugin/1.0.0/build-info-plugin-1.0.0.jar"

    @Test
    fun publishingCreatesTheMarkerAndTheJar() {
        val ws = Workspace()
        ws.run("publish")
        assertTrue(ws.file(marker).exists(), "falta el marcador $marker")
        assertTrue(ws.file(jar).exists(), "falta el jar $jar")
    }

    @Test
    fun theMarkerPointsToTheJar() {
        val ws = Workspace()
        ws.run("publish")
        val pom = ws.file(marker).readText()
        listOf("<groupId>dev.bitacora</groupId>", "<artifactId>build-info-plugin</artifactId>", "<version>1.0.0</version>").forEach {
            assertTrue(pom.contains(it), pom)
        }
    }

    @Test
    fun theJarSaysWhichClassIsThePlugin() {
        val ws = Workspace()
        ws.run("publish")
        ZipFile(ws.file(jar)).use { zip ->
            val entry = zip.getEntry("META-INF/gradle-plugins/bitacora.build-info.properties")
            val text = zip.getInputStream(entry).bufferedReader().readText()
            assertTrue(text.contains("implementation-class=BuildInfoPlugin"), text)
        }
    }

    @Test
    fun theConsumerUsesThePublishedPlugin() {
        val ws = Workspace()
        ws.run("publish")
        ws.consumer("processResources")
        assertEquals("version=3.1.0\ndomain=obra\n", ws.file("consumer/build/resources/main/build-info.properties").readText())
    }

    @Test
    fun theConsumerSearchesOnlyTheLocalRepository() {
        val ws = Workspace()
        ws.run("publish")
        ws.file("consumer/build.gradle.kts").writeText(ws.file("consumer/build.gradle.kts").readText().replace("\"1.0.0\"", "\"9.9.9\""))
        val out = ws.consumerFails("processResources").output
        assertTrue(out.contains("could not resolve plugin artifact 'bitacora.build-info:bitacora.build-info.gradle.plugin:9.9.9'"), out)
        assertFalse(out.contains("Gradle Central Plugin Repository"), "consumer no debe buscar en el Plugin Portal\n$out")
    }

    @Test
    fun aNewVersionIsANewPublication() {
        val ws = Workspace()
        ws.run("publish")
        ws.file("build.gradle.kts").writeText(ws.file("build.gradle.kts").readText().replace("\"1.0.0\"", "\"1.1.0\""))
        ws.run("publish")
        assertTrue(ws.file(marker).exists(), "publicar 1.1.0 no borra 1.0.0")
        ws.file("consumer/build.gradle.kts").writeText(ws.file("consumer/build.gradle.kts").readText().replace("\"1.0.0\"", "\"1.1.0\""))
        ws.consumer("processResources")
        assertTrue(ws.file(marker.replace("1.0.0", "1.1.0")).exists())
    }

    @Test
    fun theBuildValidatesThePlugin() {
        assertEquals(TaskOutcome.SUCCESS, Workspace().run("validatePlugins").outcome(":validatePlugins"))
    }
}

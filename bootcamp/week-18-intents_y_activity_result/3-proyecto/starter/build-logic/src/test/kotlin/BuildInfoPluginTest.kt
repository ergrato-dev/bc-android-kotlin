import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

// Ya está hecho (semana 12): los tests unitarios del plugin. Aquí van también los tuyos.
class BuildInfoPluginTest {
    @Test
    fun theExtensionExists() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("bitacora.build-info")
        assertNotNull(project.extensions.findByName("buildInfo"))
    }
}

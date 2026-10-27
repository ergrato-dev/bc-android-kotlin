import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

// Un test unitario de ejemplo (ProjectBuilder). Paso 3: escribe al menos 2 más en este archivo.
class BuildInfoPluginTest {
    @Test
    fun theExtensionExists() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("bitacora.build-info")
        assertNotNull(project.extensions.findByName("buildInfo"))
    }
}

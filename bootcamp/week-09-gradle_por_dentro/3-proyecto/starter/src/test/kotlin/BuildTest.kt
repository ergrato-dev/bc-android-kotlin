import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals

// Un test de ejemplo. Escribe al menos 6 más (paso 7 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun theZipHasTheThreeEntries() {
        val project = Project()
        project.run("dist")
        val names = ZipFile(project.file("build/dist/bitacora-0.1.0.zip")).use { zip -> zip.entries().toList().map { it.name } }
        assertEquals(listOf("README.md", "bitacora-0.1.0.jar", "build-info.properties"), names.sorted())
    }
}

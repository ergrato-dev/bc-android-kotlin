import kotlin.test.Test
import kotlin.test.assertTrue

// Un test de ejemplo. Escribe al menos 6 más (paso 5 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun aTamperedLibraryFailsTheBuild() {
        val project = Project()
        project.tamper("kotlinx-serialization-json-jvm-1.12.0.jar")
        val out = project.fail(":data:compileKotlin").output
        assertTrue(out.contains("Dependency verification failed"), out)
    }
}

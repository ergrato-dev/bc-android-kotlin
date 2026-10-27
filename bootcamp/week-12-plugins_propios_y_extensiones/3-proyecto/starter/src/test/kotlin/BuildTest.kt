import kotlin.test.Test
import kotlin.test.assertTrue

// Un test de ejemplo. Escribe al menos 6 más (paso 7 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun theAppHasTheBuildInfoTask() {
        val out = Project().run(":app:tasks", "--group", "bitacora").output
        assertTrue(out.contains("generateBuildInfo"), out)
    }
}

import kotlin.test.Test
import kotlin.test.assertTrue

// Un test de ejemplo. Escribe al menos 6 más (paso 7 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun theThreeModulesAreListed() {
        val out = Project().run("projects").output
        listOf(":app", ":core", ":data").forEach { assertTrue(out.contains("Project '$it'"), out) }
    }
}

import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

// Un test de ejemplo. Escribe al menos 6 más (paso 5 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun cleanBuildsComeFromTheCache() {
        val project = Project()
        project.run(":core:compileKotlin")
        project.run("clean")
        val result = project.run(":core:compileKotlin")
        assertEquals(TaskOutcome.FROM_CACHE, result.outcome(":core:compileKotlin"), result.output)
    }
}

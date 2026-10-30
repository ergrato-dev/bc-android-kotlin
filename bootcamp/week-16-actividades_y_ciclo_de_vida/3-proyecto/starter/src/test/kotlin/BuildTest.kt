import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Un test de ejemplo: los tests de Robolectric de app existen y pasan. Escribe al menos 6 más (paso 8 del README del
// proyecto), aquí o en app/src/test, con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun theRobolectricTestsRun() {
        val project = Project()
        assertEquals(TaskOutcome.SUCCESS, project.run(":app:testDebugUnitTest").outcome(":app:testDebugUnitTest"))
        val results = project.file("app/build/test-results/testDebugUnitTest").listFiles().orEmpty().filter { it.name.endsWith(".xml") }
        assertTrue(results.isNotEmpty())
    }
}

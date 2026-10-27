import kotlin.test.Test
import kotlin.test.assertTrue

// Un test de ejemplo. Escribe al menos 6 más (paso 7 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun coreUsesTheLibraryConvention() {
        val core = Project().file("core/build.gradle.kts").readText()
        assertTrue(core.contains("""id("bitacora.kotlin-library")"""), core)
    }
}

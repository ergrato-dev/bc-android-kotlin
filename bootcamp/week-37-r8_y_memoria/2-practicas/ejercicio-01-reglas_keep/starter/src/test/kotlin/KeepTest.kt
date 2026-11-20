import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Antes de los tests, Gradle pasa el programa por R8 (la tarea shrink). Estos tests ejecutan lo que sale y leen su
// mapping. No los cambies: cambia rules.pro.
class KeepTest {
    private val mapping = File("build/r8/mapping.txt").readLines()

    // El nombre que R8 dio a una clase: "dev.bitacora.keep.Helper -> a:" da "a".
    private fun renamed(name: String): String? =
        mapping.firstOrNull { it.startsWith("$name -> ") }?.substringAfter(" -> ")?.removeSuffix(":")

    private fun runShrunk(): Pair<Int, String> {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val process = ProcessBuilder(java, "-cp", "build/r8/shrunk.jar", "dev.bitacora.keep.MainKt").redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        return process.waitFor() to out
    }

    @Test
    fun theShrunkProgramRuns() {
        val (code, out) = runShrunk()
        assertEquals(0, code, out)
    }

    @Test
    fun theSensorsAreFound() {
        val (_, out) = runShrunk()
        assertTrue("lluvia: 12.5" in out, out)
        assertTrue("viento: 30.0" in out, out)
    }

    @Test
    fun theFieldsKeepTheirNames() {
        val (_, out) = runShrunk()
        assertTrue("code=A-1, day=2026-11-20, level=alto" in out, out)
    }

    // Lo que se carga por su nombre conserva el nombre.
    @Test
    fun theSensorsKeepTheirNames() {
        assertEquals("dev.bitacora.keep.Rain", renamed("dev.bitacora.keep.Rain"))
        assertEquals("dev.bitacora.keep.Wind", renamed("dev.bitacora.keep.Wind"))
    }

    // De Observation, la reflexión solo lee los campos: la clase puede cambiar de nombre.
    @Test
    fun observationCanBeRenamed() {
        assertFalse(renamed("dev.bitacora.keep.Observation") == "dev.bitacora.keep.Observation")
    }

    // Una regla de más también se nota: lo que el código usa de forma directa se renombra.
    @Test
    fun helperIsRenamed() {
        val name = renamed("dev.bitacora.keep.Helper")
        assertTrue(name == null || name != "dev.bitacora.keep.Helper", "$name")
    }

    // Y lo que nadie usa no está.
    @Test
    fun unusedIsGone() {
        val name = renamed("dev.bitacora.keep.Unused")
        assertTrue(name == null || name.startsWith("R8\$\$REMOVED"), "$name")
    }
}

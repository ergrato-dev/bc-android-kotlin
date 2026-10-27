import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val annotations = "org.jetbrains:annotations"

    @Test
    fun theStdlibConflictGoesToTheHighestVersion() {
        val out = Sandbox().insight("kotlin-stdlib")
        assertTrue(out.contains("By conflict resolution: between versions 2.4.21, 2.2.20 and 2.4.10"), out)
    }

    @Test
    fun theProgramStillRuns() {
        assertEquals("""{"code":"A-1","observations":3}""", Sandbox().run("run", "-q").output.trim())
    }

    @Test
    fun aConstraintRaisesAnnotations() {
        val out = Sandbox().tree("compileClasspath")
        assertTrue(out.contains("$annotations:23.0.0 -> 26.1.0"), out)
        assertTrue(out.contains("$annotations:26.1.0 (c)"), out)
    }

    @Test
    fun theConstraintSaysWhy() {
        val out = Sandbox().insight("annotations", "compileClasspath")
        val reason = out.lines().firstOrNull { it.trim().startsWith("- By constraint:") }
        assertTrue(reason != null && reason.substringAfter("By constraint:").isNotBlank(), out)
    }

    @Test
    fun theVersionLivesInTheCatalog() {
        val sb = Sandbox()
        assertTrue(sb.file("gradle/libs.versions.toml").readText().contains("26.1.0"), "La versión va en el catálogo")
        assertFalse(sb.file("build.gradle.kts").readText().contains("annotations:26.1.0"), "build.gradle.kts no repite la versión")
    }

    @Test
    fun annotationsIsNotShippedToRun() {
        val out = Sandbox().tree("runtimeClasspath")
        assertFalse(out.contains("$annotations:"), out)
    }

    @Test
    fun theDistributionHasNoAnnotationsJar() {
        val sb = Sandbox()
        sb.run("installDist")
        val jars = sb.file("build/install/bitacora/lib").list()!!.sorted()
        assertTrue("kotlinx-serialization-json-jvm-1.12.0.jar" in jars, jars.toString())
        assertTrue(jars.none { it.startsWith("annotations-") }, jars.toString())
    }
}

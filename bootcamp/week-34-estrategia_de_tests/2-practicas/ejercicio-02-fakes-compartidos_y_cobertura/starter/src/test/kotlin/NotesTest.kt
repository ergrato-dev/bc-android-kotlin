import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// No los toques: son el enunciado. Cada test copia sandbox/ y ejecuta Gradle dentro, como en la semana 33.
class NotesTest {
    private fun Sandbox.tests(module: String, name: String): Set<String> {
        val file = file("$module/build/test-results/test/TEST-dev.bitacora.notes.$name.xml")
        assertTrue(file.exists(), "no hay resultados de $name en :$module")
        return Regex("<testcase name=\"([^\"]+)\"").findAll(file.readText()).map { it.groupValues[1] }.toSet()
    }

    // El fake, una vez: en los testFixtures de :store, al lado de lo que imita.
    @Test
    fun theFakeLivesInTheFixturesOfTheStore() {
        val sandbox = Sandbox()
        val result = sandbox.run(":store:testFixturesJar")
        assertEquals(TaskOutcome.SUCCESS, result.task(":store:testFixturesJar")?.outcome)
        assertTrue(sandbox.file("store/src/testFixtures/kotlin/dev/bitacora/notes/FakeNoteStore.kt").exists())
        assertFalse(sandbox.file("report/src/test/kotlin/dev/bitacora/notes/FakeNoteStore.kt").exists())
    }

    @Test
    fun theReportTestsPass() {
        assertEquals(TaskOutcome.SUCCESS, Sandbox().run(":report:test").task(":report:test")?.outcome)
    }

    // Los mismos tests contra el fake y contra el archivo, en verde los dos.
    @Test
    fun theContractRunsAgainstTheFakeAndTheFile() {
        val sandbox = Sandbox()
        sandbox.run(":store:test")
        val fake = sandbox.tests("store", "FakeNoteStoreContractTest")
        val file = sandbox.tests("store", "FileNoteStoreContractTest")
        assertTrue(fake.size >= 4, "el contrato tiene ${fake.size} tests: $fake")
        assertEquals(fake, file)
    }

    // Lo que se empaqueta no lleva los fakes.
    @Test
    fun theFixturesAreNotShipped() {
        val runtime = Sandbox().run(":report:dependencies", "--configuration", "runtimeClasspath", "-q").output
        assertFalse("test-fixtures" in runtime, runtime)
    }

    // Un informe con los dos módulos, sin el fake.
    @Test
    fun theReportJoinsBothModules() {
        val sandbox = Sandbox()
        sandbox.run("koverXmlReport")
        val report = sandbox.file("build/reports/kover/report.xml").readText()
        assertTrue("NoteStore.kt" in report, "falta :store en el informe")
        assertTrue("Report.kt" in report, "falta :report en el informe")
        assertFalse("FakeNoteStore" in report, "el fake cuenta en la cobertura")
    }

    @Test
    fun theCoverageIsVerified() {
        val sandbox = Sandbox()
        sandbox.run("koverVerify")
        val body = (1..30).joinToString("\n") { "    if (x == $it) return ${it * 2}" }
        sandbox.file("store/src/main/kotlin/dev/bitacora/notes/Untested.kt")
            .writeText("package dev.bitacora.notes\n\nfun untested(x: Int): Int {\n$body\n    return 0\n}\n")
        val failed = sandbox.fail("koverVerify")
        assertTrue("Rule violated: lines covered percentage is" in failed.output, failed.output)
    }

    // check también la verifica: no hace falta acordarse de koverVerify.
    @Test
    fun checkVerifiesTheCoverage() {
        val result = Sandbox().run("check")
        assertEquals(TaskOutcome.SUCCESS, result.task(":koverVerify")?.outcome ?: result.task(":koverCachedVerify")?.outcome)
    }

    @Test
    fun theConfigurationCacheIsKept() {
        val sandbox = Sandbox()
        sandbox.run("koverXmlReport")
        assertTrue("Configuration cache entry reused" in sandbox.run("koverXmlReport").output)
    }
}

import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 34: la estrategia de tests, mirada desde el build. Dónde viven los fakes, qué los mantiene honestos, qué
// módulos se prueban solos y qué para el build si la cobertura baja.
class TestingTest {
    private fun Project.results(module: String) =
        File(dir, "$module/build/test-results/test").listFiles().orEmpty().filter { it.name.endsWith(".xml") }

    private fun File.tests() = Regex("<testcase name=\"([^\"]+)\"").findAll(readText()).map { it.groupValues[1] }.toSet()

    // Los fakes, una vez: en los testFixtures de :core:data, no copiados en la app.
    @Test
    fun theFakesLiveInTheTestFixturesOfData() {
        val project = Project()
        assertEquals(TaskOutcome.SUCCESS, project.run(":core:data:testFixturesJar").outcome(":core:data:testFixturesJar"))
        assertTrue(project.file("core/data/src/testFixtures/kotlin/dev/bitacora/data/testing/Fakes.kt").exists())
        assertFalse(project.file("app/src/test/kotlin/dev/bitacora/FakeRepository.kt").exists())
    }

    // testFixtures es de los tests: ni los fakes ni Turbine entran en lo que se empaqueta.
    @Test
    fun theFakesAreNotInTheApk() {
        val release = Project().run(":app:dependencies", "--configuration", "fieldReleaseRuntimeClasspath", "-q").output
        assertFalse("test-fixtures" in release, release)
        assertFalse("app.cash.turbine" in release, release)
    }

    // El mismo contrato contra el fake y contra Room: los mismos tests, en verde los dos.
    @Test
    fun theContractRunsAgainstTheFakeAndRoom() {
        val project = Project()
        project.run(":core:data:test")
        val results = project.results("core/data")
        val fake = results.single { it.name == "TEST-dev.bitacora.data.FakeRepositoryTest.xml" }.tests()
        val room = results.single { it.name == "TEST-dev.bitacora.data.RoomRepositoryTest.xml" }.tests()
        assertTrue(fake.size >= 5, "$fake")
        assertEquals(fake, room)
    }

    // :core:network se prueba en su módulo: su test ya no es NO-SOURCE.
    @Test
    fun theNetworkHasItsOwnTests() {
        assertEquals(TaskOutcome.SUCCESS, Project().run(":core:network:test").outcome(":core:network:test"))
    }

    // Un informe con los cuatro módulos core, sin lo que genera Room ni los fakes.
    @Test
    fun theReportJoinsTheCoreModules() {
        val project = Project()
        project.run("koverXmlReport")
        val report = project.file("build/reports/kover/report.xml").readText()
        for (pkg in listOf("model", "database", "network", "data")) assertTrue("<package name=\"dev/bitacora/$pkg\"" in report, pkg)
        assertFalse("_Impl" in report)
        assertFalse("FakeRepository" in report)
    }

    // koverVerify (y check) verifica la cobertura: 40 líneas sin probar en :core:model la bajan del 90% y paran el build.
    @Test
    fun lessCoverageStopsTheBuild() {
        val project = Project()
        project.run("koverVerify")
        val body = (1..40).joinToString("\n") { "    if (x == $it) return ${it * 2}" }
        project.file("core/model/src/main/kotlin/dev/bitacora/model/Untested.kt")
            .writeText("package dev.bitacora.model\n\nfun untested(x: Int): Int {\n$body\n    return 0\n}\n")
        val failed = project.fail("koverVerify")
        assertTrue("Rule violated: lines covered percentage is" in failed.output, failed.output)
    }
}

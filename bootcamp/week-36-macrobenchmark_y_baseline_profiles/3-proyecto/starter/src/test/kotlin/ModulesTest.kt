import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 33: la Bitácora en módulos. Estos tests miran el build, no la app: qué módulos hay, qué ve cada uno al
// compilar y qué se vuelve a compilar cuando cambia uno.
class ModulesTest {
    private fun Project.classpath(configuration: String) = run(":app:dependencies", "--configuration", configuration, "-q").output

    @Test
    fun theModulesAreTheOnesOfThePlan() {
        val projects = Project().run("projects", "-q").output
        for (path in listOf(":core:model", ":core:database", ":core:network", ":core:data", ":feature:map", ":app")) {
            assertTrue("Project '$path'" in projects, projects)
        }
        assertFalse("Project ':data'" in projects, projects)
    }

    // Una biblioteca de Android no tiene la tarea jar: si un módulo de core lo fuera, esto fallaría.
    @Test
    fun theCoreModulesAreKotlinWithoutAndroid() {
        Project().run(":core:model:jar", ":core:database:jar", ":core:network:jar", ":core:data:jar")
    }

    // implementation: Maps va en el APK (runtime), pero la app no lo ve al compilar. Solo ve el módulo.
    @Test
    fun theMapLibrariesStayInsideTheFeature() {
        val project = Project()
        val compile = project.classpath("fieldDebugCompileClasspath")
        assertTrue("project ':feature:map'" in compile, compile)
        assertFalse("com.google.maps.android:maps-compose" in compile, compile)
        assertTrue("com.google.maps.android:maps-compose" in project.classpath("fieldDebugRuntimeClasspath"))
    }

    // api lo que sale en las firmas (Ktor core: HttpClient); implementation lo de dentro (el JSON, las preferencias).
    @Test
    fun theCoreModulesShowOnlyWhatTheirSignaturesNeed() {
        val compile = Project().classpath("fieldDebugCompileClasspath")
        assertTrue("io.ktor:ktor-client-core" in compile, compile)
        assertFalse("io.ktor:ktor-client-content-negotiation" in compile, compile)
        assertFalse("androidx.datastore:datastore-preferences-core" in compile, compile)
    }

    // Los textos del mapa son del módulo del mapa, en los dos idiomas, y la app ya no los tiene.
    @Test
    fun theMapStringsLiveInTheFeature() {
        val project = Project()
        for (values in listOf("values", "values-en")) {
            assertTrue("name=\"on_map\"" in project.file("feature/map/src/main/res/$values/strings.xml").readText())
            assertFalse("name=\"on_map\"" in project.file("app/src/main/res/$values/strings.xml").readText())
        }
    }

    // El mapa se prueba solo, en su módulo, sin la app.
    @Test
    fun theMapHasItsOwnTests() {
        val project = Project()
        assertEquals(TaskOutcome.SUCCESS, project.run(":feature:map:testDebugUnitTest").outcome(":feature:map:testDebugUnitTest"))
        val results = File(project.dir, "feature/map/build/test-results/testDebugUnitTest").listFiles().orEmpty()
        assertTrue(results.any { it.name.endsWith(".xml") }, "sin resultados de tests en :feature:map")
    }
}

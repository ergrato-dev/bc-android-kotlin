import kotlin.test.Test
import kotlin.test.assertTrue

class RulesTest {
    private val sandbox = Sandbox()

    // Añade una dependencia al build.gradle.kts de un módulo, en un bloque dependencies propio: Gradle los junta.
    private fun depend(module: String, on: String, configuration: String = "implementation") {
        sandbox.file(module.removePrefix(":").replace(':', '/') + "/build.gradle.kts")
            .appendText("\ndependencies {\n    $configuration(project(\"$on\"))\n}\n")
    }

    private fun assertStops(message: String) {
        val output = sandbox.fail("help").output
        assertTrue(message in output, output)
    }

    @Test
    fun theGraphOfTheBitacoraIsAllowed() {
        sandbox.run("help")
    }

    @Test
    fun aFeatureCannotDependOnAnotherFeature() {
        depend(":feature:map", ":feature:notes")
        assertStops(":feature:map no puede depender de :feature:notes")
    }

    // La regla mira todas las configuraciones, también las de los tests.
    @Test
    fun theTestsFollowTheRulesToo() {
        depend(":feature:map", ":feature:notes", "testImplementation")
        assertStops(":feature:map no puede depender de :feature:notes")
    }

    @Test
    fun nobodyDependsOnTheApp() {
        depend(":core:data", ":app")
        assertStops(":core:data no puede depender de :app")
    }

    @Test
    fun theCoreDoesNotKnowTheFeatures() {
        depend(":core:data", ":feature:map")
        assertStops(":core:data no puede depender de :feature:map")
    }

    // Además sería un ciclo (:core:database ya depende de :core:model), pero la regla salta antes.
    @Test
    fun theModelDependsOnNothing() {
        depend(":core:model", ":core:database")
        assertStops(":core:model no depende de nada")
    }

    @Test
    fun aFeatureGoesThroughTheData() {
        depend(":feature:notes", ":core:database")
        assertStops(":feature:notes no puede depender de :core:database")
    }

    @Test
    fun aFeatureCanUseTheModel() {
        depend(":feature:map", ":core:model")
        sandbox.run("help")
    }

    // La app une las piezas: puede ver la base de datos para crearla (con Hilt, en la Bitácora).
    @Test
    fun theAppCanSeeTheDatabase() {
        depend(":app", ":core:database")
        sandbox.run("help")
    }

    // Un módulo nuevo sigue las reglas sin tocar nada más que su build.gradle.kts y settings.
    @Test
    fun aNewModuleFollowsTheRules() {
        sandbox.file("settings.gradle.kts").appendText("include(\":feature:settings\")\n")
        sandbox.file("feature/settings").mkdirs()
        sandbox.file("feature/settings/build.gradle.kts").writeText("plugins {\n    id(\"bitacora.module\")\n}\n")
        depend(":feature:settings", ":feature:map")
        assertStops(":feature:settings no puede depender de :feature:map")
    }

    // Las reglas corren al configurar, y la caché de configuración se sigue guardando y reutilizando.
    @Test
    fun theRulesKeepTheConfigurationCache() {
        sandbox.run("help", "--configuration-cache")
        val output = sandbox.run("help", "--configuration-cache").output
        assertTrue("Configuration cache entry reused." in output, output)
    }
}

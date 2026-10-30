import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    private fun apk(sandbox: Sandbox = Sandbox(), variant: String = "debug"): Apk {
        sandbox.run("assemble${variant.replaceFirstChar { it.uppercase() }}")
        val name = if (variant == "release") "app-release-unsigned" else "app-$variant"
        return Apk(sandbox.file("app/build/outputs/apk/$variant/$name.apk"))
    }

    private fun Apk.string(name: String, config: String = "default") =
        analyze("resources", "value", "--type", "string", "--name", name, "--config", config)

    @Test
    fun onlyTheAppLanguagesShip() {
        val configs = apk().analyze("resources", "configs", "--type", "string")
        assertEquals(listOf("default", "es", "es-r419"), configs.lines())
    }

    @Test
    fun theLocaleConfigIsGenerated() {
        val apk = apk()
        assertTrue(apk.manifest().contains("android:localeConfig="), apk.manifest())
        val config = apk.analyze("resources", "xml", "--file", "res/xml/_generated_res_locale_config.xml")
        assertTrue(config.contains("android:defaultLocale=\"en-US\""), config)
        for (locale in listOf("en-US", "es", "es-419")) {
            assertTrue(config.contains("android:name=\"$locale\""), config)
        }
    }

    @Test
    fun withoutResourcesPropertiesTheBuildFails() {
        val sandbox = Sandbox()
        sandbox.file("app/src/main/res/resources.properties").delete()
        val out = sandbox.fail("assembleDebug").output
        assertTrue(out.contains("No resources.properties file found"), out)
    }

    @Test
    fun theIconIsAdaptive() {
        val apk = apk()
        assertTrue(apk.manifest().contains("android:icon="), apk.manifest())
        val icon = apk.analyze("resources", "xml", "--file", "res/mipmap-anydpi-v21/ic_launcher.xml")
        for (layer in listOf("<adaptive-icon", "<background", "<foreground", "<monochrome")) {
            assertTrue(icon.contains(layer), icon)
        }
    }

    @Test
    fun theDebugBuildHasItsOwnName() {
        assertEquals("Field log (debug)", apk().string("app_name"))
        assertEquals("Field log", apk(variant = "release").string("app_name"))
    }

    @Test
    fun theAppNameIsTranslated() {
        assertEquals("Bitácora", apk().string("app_name", "es"))
    }

    @Test
    fun lintFindsNothingToStopTheBuild() {
        assertEquals(TaskOutcome.SUCCESS, Sandbox().run(":app:lintDebug").task(":app:lintDebug")?.outcome)
    }
}

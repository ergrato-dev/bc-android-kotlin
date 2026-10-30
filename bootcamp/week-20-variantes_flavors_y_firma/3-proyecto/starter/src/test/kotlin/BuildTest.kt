import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    private fun debugApk(project: Project = Project()): Apk {
        project.run(":app:assembleDebug")
        return Apk(project.file("app/build/outputs/apk/debug/app-debug.apk"))
    }

    // "default" es el español (values/), "en" la traducción y "es" los textos en español de AndroidX, que el filtro
    // deja pasar.
    @Test
    fun onlyTheAppLanguagesShip() {
        assertEquals(listOf("default", "en", "es"), debugApk().analyze("resources", "configs", "--type", "string").lines())
    }

    @Test
    fun theAppDeclaresItsLanguages() {
        val apk = debugApk()
        assertTrue(apk.manifest().contains("android:localeConfig="), apk.manifest())
        val config = apk.analyze("resources", "xml", "--file", "res/xml/_generated_res_locale_config.xml")
        assertTrue(config.contains("android:defaultLocale=\"es\""), config)
        assertTrue(config.contains("android:name=\"en\""), config)
    }

    @Test
    fun theIconIsAdaptiveWithAMonochromeLayer() {
        val icon = debugApk().analyze("resources", "xml", "--file", "res/mipmap-anydpi-v21/ic_launcher.xml")
        assertTrue(icon.contains("<adaptive-icon"), icon)
        assertTrue(icon.contains("<monochrome"), icon)
    }

    // La pantalla de la observación solo la abre la Bitácora.
    @Test
    fun theObservationScreenIsNotExported() {
        val manifest = debugApk().manifest()
        val activity = Regex("<activity[^>]*ObservationActivity[^>]*>").find(manifest)?.value
        assertEquals(true, activity?.contains("android:exported=\"false\""), manifest)
    }

    // Solo la ubicación. Sin CAMERA, porque la foto la hace la app de la cámara; el último es de androidx.core.
    @Test
    fun onlyTheLocationPermissionsShip() {
        assertEquals(
            listOf(
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION",
                "dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            ),
            debugApk().analyze("manifest", "permissions").lines().sorted(),
        )
    }

    @Test
    fun aPhoneWithoutGpsCanInstallIt() {
        val badging = debugApk().badging()
        assertTrue(badging.contains("uses-feature-not-required: name='android.hardware.location'"), badging)
        assertTrue(!badging.contains("uses-feature: name='android.hardware.location'"), badging)
    }

    @Test
    fun photosAreSharedThroughAFileProvider() {
        val manifest = debugApk().manifest()
        val provider = Regex("<provider[^>]*FileProvider[^>]*>").find(manifest)?.value ?: error(manifest)
        assertTrue(provider.contains("android:authorities=\"dev.bitacora.agro.files\""), provider)
        assertTrue(provider.contains("android:exported=\"false\""), provider)
    }

    @Test
    fun lintPassesAndStopsAMissingTranslation() {
        val project = Project()
        project.run(":app:lintDebug")
        val strings = project.file("app/src/main/res/values/strings.xml")
        strings.writeText(strings.readText().replace("</resources>", "    <string name=\"untranslated\">Sin traducir</string>\n</resources>"))
        val out = project.fail(":app:lintDebug").output
        assertTrue(out.contains("[MissingTranslation]"), out)
    }

    // Ejemplo: las dos de debug se instalan una al lado de la otra, y de la publicada. Empieza en rojo.
    @Test
    fun theDemoAndTheFieldAppCanLiveTogether() {
        val project = Project()
        project.run(":app:assembleDebug")
        val field = Apk(project.file("app/build/outputs/apk/field/debug/app-field-debug.apk"))
        val demo = Apk(project.file("app/build/outputs/apk/demo/debug/app-demo-debug.apk"))
        assertEquals("dev.bitacora.agro.debug", field.analyze("manifest", "application-id"))
        assertEquals("dev.bitacora.agro.demo.debug", demo.analyze("manifest", "application-id"))
        assertEquals("1.0.0-demo-debug", demo.analyze("manifest", "version-name"))
    }
}

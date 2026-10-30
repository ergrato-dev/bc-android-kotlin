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

    // Ejemplo: solo la ubicación. Sin CAMERA, porque la foto la hace la app de la cámara; el último es de androidx.core.
    // Empieza en rojo.
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
    fun lintPassesAndStopsAMissingTranslation() {
        val project = Project()
        project.run(":app:lintDebug")
        val strings = project.file("app/src/main/res/values/strings.xml")
        strings.writeText(strings.readText().replace("</resources>", "    <string name=\"untranslated\">Sin traducir</string>\n</resources>"))
        val out = project.fail(":app:lintDebug").output
        assertTrue(out.contains("[MissingTranslation]"), out)
    }
}

import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Ya está hecho. Antes de estos tests, Gradle construye el AAR de :sensors, la app en release y en debug, y el APK de
// sus tests del dispositivo (build.gradle.kts del raíz). Los tests miran lo que sale. No los cambies.
class ShrinkTest {
    private val release = File("app/build/outputs/apk/release/app-release-unsigned.apk")
    private val debug = File("app/build/outputs/apk/debug/app-debug.apk")
    private val androidTest = File("app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk")
    private val mapping = File("app/build/outputs/mapping/release/mapping.txt")

    // El texto de todos los dex de un APK: los nombres de las clases que usa están ahí, como Lpaquete/Clase;.
    private fun dex(apk: File): String = ZipFile(apk).use { zip ->
        zip.entries().asSequence().filter { it.name.endsWith(".dex") }
            .joinToString("") { zip.getInputStream(it).readBytes().toString(Charsets.ISO_8859_1) }
    }

    // La tabla de recursos de un APK, con aapt2 (build-tools): `aapt2 dump resources app.apk`.
    private fun resources(apk: File): String {
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "build-tools/36.0.0/aapt2").path, "dump", "resources", apk.path).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        return out
    }

    @Test
    fun theReleaseGoesThroughR8() {
        assertTrue(mapping.isFile, "No hay mapping: la release no pasa por R8")
    }

    // La regla de las sondas viaja dentro del AAR, en proguard.txt.
    @Test
    fun theLibraryShipsItsRule() {
        val rules = ZipFile("sensors/build/outputs/aar/sensors-release.aar").use { zip ->
            zip.getEntry("proguard.txt")?.let { zip.getInputStream(it).readBytes().decodeToString() }
        }
        assertTrue(rules != null && "dev.bitacora.sensors.Probe" in rules, "$rules")
    }

    // La app no sabe que Sensors usa reflexión: sus reglas no hablan de las sondas.
    @Test
    fun theAppDoesNotKnowAboutTheRule() {
        val rules = File("app/proguard-rules.pro").takeIf { it.isFile }?.readText().orEmpty()
        assertFalse("Probe" in rules || "Humidity" in rules, rules)
    }

    // Se crean por su nombre: lo conservan. Sensors, que el código nombra, puede cambiarlo.
    @Test
    fun theProbesKeepTheirNames() {
        val classes = mapping.readLines().filter { !it.startsWith(" ") && !it.startsWith("#") }
        assertTrue("dev.bitacora.Humidity -> dev.bitacora.Humidity:" in classes)
        assertTrue("dev.bitacora.Temperature -> dev.bitacora.Temperature:" in classes)
        assertFalse("dev.bitacora.sensors.Sensors -> dev.bitacora.sensors.Sensors:" in classes)
    }

    // El logo de antes no lo usa nadie: en debug está; en la release, no.
    @Test
    fun theOldLogoIsGone() {
        assertTrue("drawable/old_logo" in resources(debug))
        assertFalse("drawable/old_logo" in resources(release))
    }

    // LeakCanary, en la debug y en sus tests; en la release, nada.
    @Test
    fun leakCanaryIsOnlyInDebug() {
        assertTrue("Lleakcanary/" in dex(debug), "La debug no lleva LeakCanary")
        assertFalse("Lleakcanary/" in dex(release))
        assertTrue("Lleakcanary/DetectLeaksAfterTestSuccess;" in dex(androidTest))
    }
}

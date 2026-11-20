import java.io.File
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// No los toques: son el enunciado. Miran lo que deja el build; ./gradlew test construye antes los APK que hacen falta.
class ProfileTest {
    private val rules = File("app/src/main/baseline-prof.txt")
    private val merged = File("app/build/intermediates/merged_art_profile/release/mergeReleaseArtProfile/baseline-prof.txt")
    private val release = File("app/build/outputs/apk/release/app-release-unsigned.apk")
    private val measured = File("app/build/outputs/apk/benchmarkRelease/app-benchmarkRelease.apk")
    private val benchmark = File("benchmark/build/outputs/apk/benchmarkRelease/benchmark-benchmarkRelease.apk")

    // Ya está hecho: el manifiesto de un APK, con apkanalyzer (Android SDK, cmdline-tools). Necesita ANDROID_HOME.
    private fun manifest(apk: File): String {
        assertTrue(apk.isFile, "No existe $apk")
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder("$home/cmdline-tools/latest/bin/apkanalyzer", "manifest", "print", apk.path).redirectErrorStream(true).start()
        return process.inputStream.bufferedReader().readText().also { process.waitFor() }
    }

    private fun ZipFile.dexContains(text: String) = entries().toList()
        .filter { it.name.endsWith(".dex") }
        .any { text in getInputStream(it).readBytes().decodeToString() }

    // Una regla es una clase (Lpaquete/Clase;) o un método (con ->), con las marcas H, S y P delante si es un método.
    @Test
    fun everyRuleIsWellFormed() {
        val lines = rules.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        assertTrue(lines.isNotEmpty())
        val rule = Regex("""(L[^;]+;)|([HSP]+L[^;]+;->[^(]+\([^)]*\).+)""")
        for (line in lines) assertTrue(rule.matches(line), line)
    }

    // Las reglas escritas a mano llegan al perfil fusionado, junto a las de Compose y las demás bibliotecas.
    @Test
    fun theRulesReachTheMergedProfile() {
        val lines = merged.readLines()
        assertTrue(lines.any { it.startsWith("HSPLdev/bitacora/PlotsKt;") }, "${lines.size} líneas")
        assertTrue(lines.any { it.startsWith("Landroidx/compose/") })
    }

    @Test
    fun theProfileIsInTheApk() {
        ZipFile(release).use { assertTrue(it.getEntry("assets/dexopt/baseline.prof") != null) }
    }

    // Quien compila el perfil en un móvil que no lo recibe de Play.
    @Test
    fun theAppHasTheProfileInstaller() {
        assertTrue("androidx.profileinstaller.ProfileInstallReceiver" in manifest(release))
    }

    // El APK de los benchmarks se instrumenta a sí mismo: maneja la app desde fuera.
    @Test
    fun theBenchmarkInstrumentsItself() {
        assertTrue("android:targetPackage=\"dev.bitacora.benchmark\"" in manifest(benchmark))
    }

    // Lo que se mide es una release que Macrobenchmark puede trazar desde la shell, no una debug.
    @Test
    fun theMeasuredAppIsAProfileableRelease() {
        val manifest = manifest(measured)
        assertTrue("android:shell=\"true\"" in manifest, manifest)
        assertFalse("android:debuggable=\"true\"" in manifest, manifest)
    }

    // Macrobenchmark y UiAutomator, solo en el APK de los benchmarks.
    @Test
    fun theBenchmarkIsNotInTheApp() {
        ZipFile(release).use {
            assertFalse(it.dexContains("Landroidx/benchmark/"))
            assertFalse(it.dexContains("Landroidx/test/uiautomator/"))
        }
    }
}

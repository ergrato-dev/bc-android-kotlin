import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 36: el rendimiento, mirado desde el build. Los benchmarks en su módulo, el Baseline Profile generado y en el
// repositorio, que llega al APK, y lo que hace falta para medir y generar sin tocar la app que se publica.
class PerformanceTest {
    private val profiles = "app/src/fieldRelease/generated/baselineProfiles"
    private val onCreate = "Ldev/bitacora/MainActivity;->onCreate(Landroid/os/Bundle;)V"

    // Un módulo de test (com.android.test). Su APK se instrumenta a sí mismo, no a la app: la maneja desde fuera, como
    // una persona. Para ejecutarlo, Gradle empaqueta antes la app de field en benchmarkRelease (targetProjectPath).
    @Test
    fun theBenchmarksAreAModuleOfTheirOwn() {
        val project = Project()
        project.run(":baselineprofile:assembleBenchmarkRelease")
        val manifest = Apk(project.file("baselineprofile/build/outputs/apk/benchmarkRelease/baselineprofile-benchmarkRelease.apk")).manifest()
        assertTrue("android:targetPackage=\"dev.bitacora.baselineprofile\"" in manifest, manifest)
        val plan = project.run(":baselineprofile:connectedBenchmarkReleaseAndroidTest", "-m").output
        assertTrue(":app:packageFieldBenchmarkRelease SKIPPED" in plan, plan)
    }

    // El perfil generado va con el código, como las capturas: con reglas de la app, no solo de las bibliotecas.
    @Test
    fun theProfileIsInTheRepository() {
        val rules = Project().file("$profiles/baseline-prof.txt").readLines()
        assertTrue(rules.count { "Ldev/bitacora/" in it } >= 100, "${rules.size} reglas")
        assertTrue(rules.any { it.endsWith(onCreate) })
    }

    // El de arranque es otro recorrido: solo hasta el primer fotograma, más corto que el Baseline Profile.
    @Test
    fun thereIsAStartupProfile() {
        val project = Project()
        val startup = project.file("$profiles/startup-prof.txt").readLines()
        val baseline = project.file("$profiles/baseline-prof.txt").readLines()
        assertTrue(startup.any { it.endsWith(onCreate) })
        assertTrue(startup.size < baseline.size, "${startup.size} y ${baseline.size}")
    }

    // Las reglas llegan al APK que se publica, junto a las de las bibliotecas, en assets/dexopt.
    @Test
    fun theProfileReachesTheApk() {
        val project = Project()
        project.run(":app:assembleFieldRelease")
        val merged = project.file("app/build/intermediates/merged_art_profile/fieldRelease/mergeFieldReleaseArtProfile/baseline-prof.txt")
        assertTrue(merged.readLines().any { it.endsWith(onCreate) })
        val apk = project.file("app/build/outputs/apk/field/release/app-field-release-unsigned.apk")
        assertTrue(java.util.zip.ZipFile(apk).use { it.getEntry("assets/dexopt/baseline.prof") != null })
    }

    // build compila los benchmarks y la app que miden, la de field: la demo no se publica, ni se mide.
    @Test
    fun buildAssemblesTheBenchmarks() {
        val plan = Project().run("build", "-m").output
        assertTrue(":baselineprofile:assembleBenchmarkRelease" in plan, plan)
        assertTrue(":app:assembleFieldBenchmarkRelease" in plan, plan)
        assertFalse(":app:assembleDemoBenchmarkRelease" in plan, plan)
    }

    // El perfil se genera en un emulador que Gradle gestiona, no en el móvil que haya conectado.
    @Test
    fun generatingUsesTheManagedDevice() {
        val plan = Project().run(":app:generateBaselineProfile", "-m").output
        assertTrue(":baselineprofile:pixel6Api36NonMinifiedReleaseAndroidTest SKIPPED" in plan, plan)
    }

    // profileinstaller va en la app; Macrobenchmark y UiAutomator, solo en el APK de los benchmarks.
    @Test
    fun theBenchmarkLibrariesAreNotInTheApk() {
        val release = Project().run(":app:dependencies", "--configuration", "fieldReleaseRuntimeClasspath", "-q").output
        assertTrue("androidx.profileinstaller:profileinstaller" in release)
        for (library in listOf("benchmark-macro", "uiautomator")) {
            assertFalse(library in release, library)
        }
    }
}

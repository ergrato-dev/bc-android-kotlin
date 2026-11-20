import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 35: los tests de UI, mirados desde el build. Las capturas de referencia en el repositorio y comparadas en
// cada test, Espresso-Intents para compartir, los tests instrumentados compilados en check y un dispositivo
// gestionado para ejecutarlos.
class UiTest {
    private fun Project.screenshots() =
        file("app/src/test/screenshots").listFiles().orEmpty().map { it.name }.filter { it.endsWith(".png") }

    private fun Project.results(test: String) = file("app/build/test-results/testFieldDebugUnitTest/TEST-dev.bitacora.$test.xml")

    // Las capturas de referencia van con el código: al menos cuatro, de ScreenshotTest.
    @Test
    fun theScreenshotsAreInTheRepository() {
        val names = Project().screenshots()
        assertTrue(names.count { it.startsWith("dev.bitacora.ScreenshotTest.") } >= 4, "$names")
    }

    // ./gradlew test compara con ellas sin grabar nada: en verde, y sin imágenes de diferencias.
    @Test
    fun theTestsCompareTheScreenshots() {
        val project = Project()
        val before = project.screenshots()
        project.run(":app:testFieldDebugUnitTest", "--tests", "dev.bitacora.ScreenshotTest")
        assertTrue(project.results("ScreenshotTest").exists())
        assertEquals(before, project.screenshots())
        val outputs = project.file("app/build/outputs/roborazzi").listFiles().orEmpty().map { it.name }
        assertFalse(outputs.any { "_compare" in it }, "$outputs")
    }

    // Otro color para el nivel alto: las aserciones no lo ven, la captura sí, y para el build.
    @Test
    fun aChangedColorFailsTheTests() {
        val project = Project()
        val colors = project.file("app/src/main/res/values/colors.xml")
        colors.writeText(colors.readText().replace("#FFF4C7C3", "#FF00FF00"))
        val failed = project.fail(":app:testFieldDebugUnitTest", "--tests", "dev.bitacora.ScreenshotTest")
        assertTrue("ScreenshotTest" in failed.output, failed.output)
        assertTrue("is changed" in project.results("ScreenshotTest").readText())
    }

    // Lo que sale de la app al compartir se comprueba con Espresso-Intents, en la JVM.
    @Test
    fun shareIsCheckedWithIntents() {
        val project = Project()
        project.run(":app:testFieldDebugUnitTest", "--tests", "dev.bitacora.ShareTest")
        val results = project.results("ShareTest").readText()
        assertTrue("<testcase" in results)
        assertFalse("<failure" in results)
        assertTrue(project.file("app/src/testField/kotlin/dev/bitacora/ShareTest.kt").readText().contains("intended("))
    }

    // check compila los tests instrumentados: un androidTest roto para el build sin necesitar un dispositivo.
    @Test
    fun checkCompilesTheInstrumentedTests() {
        val project = Project()
        val plan = project.run(":app:check", "-m").output
        assertTrue(":app:assembleFieldDebugAndroidTest" in plan, plan)
        val built = project.run(":app:assembleFieldDebugAndroidTest")
        assertEquals(TaskOutcome.SUCCESS, built.outcome(":app:compileFieldDebugAndroidTestKotlin"))
        assertTrue(project.file("app/build/outputs/apk/androidTest/field/debug/app-field-debug-androidTest.apk").exists())
    }

    // Un emulador que Gradle crea y arranca solo: la tarea existe (ejecutarla necesita KVM).
    @Test
    fun thereIsAManagedDevice() {
        val plan = Project().run(":app:pixel6Api36FieldDebugAndroidTest", "-m").output
        assertTrue(":app:pixel6Api36FieldDebugAndroidTest SKIPPED" in plan, plan)
    }

    // Roborazzi, Espresso, el runner y Hilt de los tests no entran en el APK.
    @Test
    fun theTestLibrariesAreNotInTheApk() {
        val release = Project().run(":app:dependencies", "--configuration", "fieldReleaseRuntimeClasspath", "-q").output
        for (library in listOf("roborazzi", "espresso", "androidx.test:runner", "hilt-android-testing")) {
            assertFalse(library in release, library)
        }
    }
}

import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    private fun Sandbox.debugApk(): Apk {
        run(":app:assembleDebug")
        return Apk(file("app/build/outputs/apk/debug/app-debug.apk"))
    }

    @Test
    fun coreTestsRunOnTheJvm() {
        val result = Sandbox().run(":core:test")
        assertEquals(TaskOutcome.SUCCESS, result.task(":core:test")?.outcome)
        assertTrue(Sandbox().file("core/build.gradle.kts").readText().contains("kotlin.jvm"))
    }

    @Test
    fun coreIsInTheDex() {
        val classes = Sandbox().debugApk().analyze("dex", "packages")
        assertTrue(classes.lines().any { it.startsWith("C d") && it.endsWith("dev.bitacora.core.GreetingKt") }, classes)
    }

    @Test
    fun theApkHasTheBuildInfo() {
        assertEquals("version=1.0.0\ndomain=agro\n", Sandbox().debugApk().entry("assets/build-info.properties"))
    }

    @Test
    fun theReleaseApkHasItToo() {
        val sb = Sandbox()
        sb.run(":app:assembleRelease")
        val apk = Apk(sb.file("app/build/outputs/apk/release/app-release-unsigned.apk"))
        assertEquals("version=1.0.0\ndomain=agro\n", apk.entry("assets/build-info.properties"))
    }

    @Test
    fun aNewVersionChangesTheAssetAndTheManifest() {
        val sb = Sandbox()
        sb.file("gradle.properties").writeText("version=1.1.0\n")
        val apk = sb.debugApk()
        assertEquals("version=1.1.0\ndomain=agro\n", apk.entry("assets/build-info.properties"))
        assertEquals("1.1.0", apk.analyze("manifest", "version-name"))
    }

    @Test
    fun theTaskRunsOnceAndIsUpToDateAfter() {
        val sb = Sandbox()
        assertEquals(TaskOutcome.SUCCESS, sb.run(":app:assembleDebug").task(":app:generateBuildInfo")?.outcome)
        assertEquals(TaskOutcome.UP_TO_DATE, sb.run(":app:assembleDebug").task(":app:generateBuildInfo")?.outcome)
    }

    @Test
    fun theOutputDirIsChosenByAgp() {
        val sb = Sandbox()
        sb.run(":app:generateBuildInfo")
        assertTrue(sb.file("app/build/generated/assets/generateBuildInfo/build-info.properties").isFile)
    }
}

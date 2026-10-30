import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildTest {
    private fun debugApk(): Apk {
        val sb = Sandbox()
        sb.run("assembleDebug")
        return Apk(sb.file("app/build/outputs/apk/debug/app-debug.apk"))
    }

    @Test
    fun theApplicationIdIsTheDomain() {
        assertEquals("dev.bitacora.agro", debugApk().analyze("manifest", "application-id"))
    }

    @Test
    fun theSdkLevels() {
        val apk = debugApk()
        assertEquals("26", apk.analyze("manifest", "min-sdk"))
        assertEquals("36", apk.analyze("manifest", "target-sdk"))
        assertTrue(apk.manifest().contains("android:compileSdkVersion=\"37\""), apk.manifest())
    }

    @Test
    fun theVersion() {
        val apk = debugApk()
        assertEquals("1.0.0", apk.analyze("manifest", "version-name"))
        assertEquals("1", apk.analyze("manifest", "version-code"))
    }

    @Test
    fun theAppHasAName() {
        val manifest = debugApk().manifest()
        assertTrue(manifest.contains("android:label=\"Bitácora\""), manifest)
    }

    @Test
    fun theActivityIsInTheLauncher() {
        val manifest = debugApk().manifest()
        assertTrue(manifest.contains("android:name=\"dev.bitacora.MainActivity\""), manifest)
        assertTrue(manifest.contains("android:exported=\"true\""), manifest)
        assertTrue(manifest.contains("android.intent.action.MAIN"), manifest)
        assertTrue(manifest.contains("android.intent.category.LAUNCHER"), manifest)
    }

    @Test
    fun theActivityIsInTheDex() {
        val classes = debugApk().analyze("dex", "packages")
        assertTrue(classes.lines().any { it.startsWith("C d") && it.endsWith("dev.bitacora.MainActivity") }, classes)
    }

    @Test
    fun kotlinIs2421() {
        val out = Sandbox().run("buildEnvironment").output
        assertTrue(out.contains("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10 -> 2.4.21"), out)
    }
}

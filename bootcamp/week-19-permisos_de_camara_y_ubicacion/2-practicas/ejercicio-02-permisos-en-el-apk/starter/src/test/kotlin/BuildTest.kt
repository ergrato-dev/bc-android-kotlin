import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private fun debugApk(sandbox: Sandbox = Sandbox()): Apk {
        sandbox.run("assembleDebug")
        return Apk(sandbox.file("app/build/outputs/apk/debug/app-debug.apk"))
    }

    // El último es de androidx.core: un permiso propio de la app, con su applicationId, que nadie más puede tener.
    @Test
    fun onlyThePermissionsTheAppUsesShip() {
        val permissions = debugApk().analyze("manifest", "permissions").lines().sorted()
        assertEquals(
            listOf(
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.CAMERA",
                "android.permission.INTERNET",
                "dev.bitacora.agro.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            ),
            permissions,
        )
    }

    @Test
    fun theCameraIsOptional() {
        val badging = debugApk().badging()
        assertTrue(badging.contains("uses-feature-not-required: name='android.hardware.camera'"), badging)
        assertFalse(badging.contains("uses-feature: name='android.hardware.camera'"), badging)
    }

    @Test
    fun theLocationIsOptional() {
        val badging = debugApk().badging()
        assertTrue(badging.contains("uses-feature-not-required: name='android.hardware.location'"), badging)
        assertFalse(badging.contains("uses-feature: name='android.hardware.location'"), badging)
    }

    @Test
    fun photosAreSharedThroughAFileProvider() {
        val manifest = debugApk().manifest()
        val provider = Regex("<provider[^>]*FileProvider[^>]*>").find(manifest)?.value ?: error(manifest)
        assertTrue(provider.contains("android:authorities=\"dev.bitacora.agro.files\""), provider)
        assertTrue(provider.contains("android:exported=\"false\""), provider)
        assertTrue(provider.contains("android:grantUriPermissions=\"true\""), provider)
    }

    @Test
    fun onlyThePhotosFolderIsShared() {
        val paths = debugApk().analyze("resources", "xml", "--file", "res/xml/file_paths.xml")
        assertTrue(paths.contains("<files-path"), paths)
        assertTrue(paths.contains("name=\"photos\""), paths)
        assertTrue(paths.contains("path=\"photos/\""), paths)
    }

    @Test
    fun lintFindsNothingToStopTheBuild() {
        assertEquals(TaskOutcome.SUCCESS, Sandbox().run(":app:lintDebug").task(":app:lintDebug")?.outcome)
    }

    // Una llamada que necesita la ubicación, sin comprobar el permiso: lint la para.
    @Test
    fun lintStopsALocationCallWithoutACheck() {
        val sandbox = Sandbox()
        sandbox.file("app/src/main/kotlin/dev/bitacora/Spy.kt").writeText(
            """
            package dev.bitacora

            import android.content.Context
            import android.location.LocationManager

            fun spy(context: Context) = context.getSystemService(LocationManager::class.java).getLastKnownLocation(LocationManager.GPS_PROVIDER)
            """.trimIndent(),
        )
        val out = sandbox.fail(":app:lintDebug").output
        assertTrue(out.contains("[MissingPermission]"), out)
    }
}

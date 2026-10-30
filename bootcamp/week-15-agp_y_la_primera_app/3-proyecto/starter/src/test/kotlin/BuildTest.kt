import kotlin.test.Test
import kotlin.test.assertEquals

// Un test de ejemplo. Escribe al menos 6 más (paso 10 del README del proyecto), con el valor esperado escrito a mano.
class BuildTest {
    @Test
    fun theApkHasTheBuildInfo() {
        val project = Project()
        project.run(":app:assembleDebug")
        val apk = Apk(project.file("app/build/outputs/apk/debug/app-debug.apk"))
        assertEquals("version=1.0.0\ndomain=agro\n", apk.entry("assets/build-info.properties"))
    }
}

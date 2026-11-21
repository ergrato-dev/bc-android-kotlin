import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Semana 37: R8 en la release, y lo que hace falta para leer lo que sale. LeakCanary, solo en debug.
class ShrinkTest {
    private val mapping = "app/build/outputs/mapping/fieldRelease/mapping.txt"

    // La release pasa por R8: quita código y recursos sin usar, y renombra. Con todo, el APK de field ocupaba 13,2 MB.
    @Test
    fun theReleaseIsShrunk() {
        val project = Project()
        project.run(":app:assembleFieldRelease")
        assertTrue(project.file(mapping).isFile)
        val apk = project.file("app/build/outputs/apk/field/release/app-field-release-unsigned.apk")
        assertTrue(apk.length() < 4_000_000, "${apk.length()} bytes")
    }

    // La debug no: compila antes, y el depurador ve los nombres de verdad.
    @Test
    fun theDebugIsNotShrunk() {
        val plan = Project().run(":app:assembleFieldDebug", "-m").output
        assertFalse("minifyFieldDebugWithR8" in plan, plan)
    }

    // Lo que se mide es lo que se publica: benchmarkRelease pasa por R8; el perfil se genera sin él, con los nombres
    // del código.
    @Test
    fun theBenchmarksMeasureTheShrunkApp() {
        val project = Project()
        assertTrue(":app:minifyFieldBenchmarkReleaseWithR8" in project.run(":app:assembleFieldBenchmarkRelease", "-m").output)
        assertFalse(":app:minifyFieldNonMinifiedReleaseWithR8" in project.run(":app:assembleFieldNonMinifiedRelease", "-m").output)
    }

    // WorkManager guarda en su base de datos el nombre de la clase del worker. Si R8 lo cambiara, el trabajo pendiente
    // de la versión anterior no encontraría su clase. Las reglas de work-runtime lo conservan; lo demás, renombrado.
    @Test
    fun theWorkerKeepsItsName() {
        val project = Project()
        project.run(":app:assembleFieldRelease")
        val classes = project.file(mapping).readLines().filter { !it.startsWith(" ") && !it.startsWith("#") }
        assertTrue("dev.bitacora.SyncWorker -> dev.bitacora.SyncWorker:" in classes)
        val viewModel = classes.single { it.startsWith("dev.bitacora.BitacoraViewModel -> ") }
        assertFalse(viewModel.endsWith("-> dev.bitacora.BitacoraViewModel:"), viewModel)
    }

    // Con el mapping, retrace devuelve una traza ofuscada a los nombres y las líneas del código. La línea ofuscada
    // sale del propio mapping: "5:13:void onIntent(...):64:64 -> e" dice que las líneas 5 a 13 de e son la 64.
    @Test
    fun aTraceCanBeRetraced() {
        val project = Project()
        project.run(":app:assembleFieldRelease")
        val lines = project.file(mapping).readLines()
        val start = lines.indexOfFirst { it.startsWith("dev.bitacora.BitacoraViewModel -> ") }
        val obfuscated = lines[start].substringAfter(" -> ").removeSuffix(":")
        val member = Regex("""^\s+(\d+):\d+:void onIntent\(dev\.bitacora\.BitacoraIntent\):(\d+):\d+ -> (\w+)$""")
        val (line, original, method) = lines.drop(start + 1).takeWhile { it.startsWith(" ") || it.startsWith("#") }
            .firstNotNullOf { member.find(it)?.destructured }
        val trace = File.createTempFile("trace", ".txt").apply { writeText("\tat $obfuscated.$method(SourceFile:$line)\n") }
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "cmdline-tools/latest/bin/retrace").path, project.file(mapping).path, trace.path)
            .redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.waitFor(), out)
        assertTrue("at dev.bitacora.BitacoraViewModel.onIntent(BitacoraViewModel.kt:$original)" in out, out)
    }

    // LeakCanary vigila la debug (y los tests del dispositivo); en la release no está.
    @Test
    fun leakCanaryIsOnlyInDebug() {
        val project = Project()
        val debug = project.run(":app:dependencies", "--configuration", "fieldDebugRuntimeClasspath", "-q").output
        val release = project.run(":app:dependencies", "--configuration", "fieldReleaseRuntimeClasspath", "-q").output
        assertTrue("com.squareup.leakcanary:leakcanary-android" in debug)
        assertFalse("leakcanary" in release, release)
        // Y sus permisos (guardar el volcado del heap), tampoco.
        project.run(":app:assembleFieldRelease")
        val permissions = Apk(project.file("app/build/outputs/apk/field/release/app-field-release-unsigned.apk")).analyze("manifest", "permissions")
        assertFalse("WRITE_EXTERNAL_STORAGE" in permissions, permissions)
    }
}

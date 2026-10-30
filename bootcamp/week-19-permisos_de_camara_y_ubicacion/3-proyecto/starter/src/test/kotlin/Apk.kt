import java.io.File
import java.util.zip.ZipFile

// Ya está hecho: pregunta a apkanalyzer (Android SDK, cmdline-tools) por un APK, como harías tú en la terminal:
// `apkanalyzer manifest application-id app-debug.apk`. Necesita ANDROID_HOME.
class Apk(val file: File) {
    init {
        check(file.isFile) { "No existe el APK $file" }
    }

    private val tool: String by lazy {
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        File(home, "cmdline-tools/latest/bin/apkanalyzer").path
    }

    fun analyze(vararg args: String): String {
        val process = ProcessBuilder(tool, *args, file.path).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        return out.trim()
    }

    fun manifest(): String = analyze("manifest", "print")

    // Lo que ve Google Play al publicar: permisos y funciones del hardware, también las que se deducen de los
    // permisos. Es `aapt2 dump badging`, de build-tools.
    fun badging(): String {
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "build-tools/36.0.0/aapt2").path, "dump", "badging", file.path).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        return out
    }

    // Un archivo de dentro del APK, como texto (assets/..., por ejemplo).
    fun entry(path: String): String? = ZipFile(file).use { zip -> zip.getEntry(path)?.let { zip.getInputStream(it).readBytes().decodeToString() } }
}

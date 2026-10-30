import java.io.File

// Ya está hecho: pregunta a apkanalyzer (Android SDK, cmdline-tools) por un APK, como harías tú en la terminal:
// `apkanalyzer manifest application-id app-field-debug.apk`. Necesita ANDROID_HOME.
class Apk(val file: File) {
    init {
        check(file.isFile) { "No existe el APK $file" }
    }

    private val home: String by lazy { System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android") }

    fun analyze(vararg args: String): String {
        val process = ProcessBuilder(File(home, "cmdline-tools/latest/bin/apkanalyzer").path, *args, file.path).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        return out.trim()
    }

    // La firma, como la ve Android al instalar: `apksigner verify --print-certs -v`, de build-tools. Un APK sin firmar
    // no se verifica, y apksigner lo dice en la salida.
    fun signature(): String {
        val process = ProcessBuilder(File(home, "build-tools/36.0.0/apksigner").path, "verify", "--print-certs", "-v", file.path).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return out
    }
}

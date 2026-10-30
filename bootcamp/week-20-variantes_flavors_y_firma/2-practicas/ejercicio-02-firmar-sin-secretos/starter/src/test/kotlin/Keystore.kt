import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: una clave de subida de usar y tirar, creada con keytool (del JDK) fuera del proyecto, como la tuya.
object Keystore {
    const val PASSWORD = "not-in-the-repo"
    const val ALIAS = "upload"

    val file: File by lazy {
        val dir = createTempDirectory("keys").toFile()
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
        val file = File(dir, "upload.jks")
        val keytool = File(System.getProperty("java.home"), "bin/keytool").path
        val process = ProcessBuilder(
            keytool, "-genkeypair", "-keystore", file.path, "-alias", ALIAS, "-keyalg", "RSA", "-keysize", "2048",
            "-validity", "10000", "-storepass", PASSWORD, "-keypass", PASSWORD, "-dname", "CN=Bitacora upload, O=Bootcamp, C=ES",
        ).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        file
    }

    fun properties(password: String = PASSWORD) = mapOf(
        "uploadStoreFile" to file.path,
        "uploadStorePassword" to password,
        "uploadKeyAlias" to ALIAS,
        "uploadKeyPassword" to password,
    )
}

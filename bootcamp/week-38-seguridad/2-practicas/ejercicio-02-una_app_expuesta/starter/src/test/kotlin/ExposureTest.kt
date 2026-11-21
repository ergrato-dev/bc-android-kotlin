import org.w3c.dom.Element
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.util.Base64
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Ya está hecho. Antes de estos tests, Gradle construye la app en release y en debug, y pasa lint por la release
// (build.gradle.kts del raíz). Los tests miran lo que sale. No los cambies.
class ExposureTest {
    private val release = File("app/build/outputs/apk/release/app-release-unsigned.apk")
    private val debug = File("app/build/outputs/apk/debug/app-debug.apk")
    private val android = "http://schemas.android.com/apk/res/android"

    // apkanalyzer, de cmdline-tools: lee el manifiesto y los XML compilados de un APK.
    private fun apkanalyzer(vararg args: String): String {
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "cmdline-tools/latest/bin/apkanalyzer").path, *args).redirectErrorStream(true).start()
        val out = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { out }
        return out
    }

    private fun parse(xml: String): Element =
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(xml.byteInputStream()).documentElement

    private fun Element.children(tag: String): List<Element> =
        getElementsByTagName(tag).let { list -> (0 until list.length).map { list.item(it) as Element } }

    private fun manifest(apk: File) = parse(apkanalyzer("manifest", "print", apk.path))

    // La ruta de la configuración de red en el APK (en la release, acortada: res/8G.xml), o null si el manifiesto no la
    // nombra. Sale de la tabla de recursos.
    private fun networkConfigPath(apk: File): String? {
        if (!manifest(apk).children("application").single().hasAttributeNS(android, "networkSecurityConfig")) return null
        return apkanalyzer("resources", "value", "--type", "xml", "--config", "default", "--name", "network_security_config", apk.path)
            .lines().firstOrNull { it.startsWith("res/") }
    }

    private fun networkConfig(apk: File): Element? = networkConfigPath(apk)?.let { parse(apkanalyzer("resources", "xml", "--file", it, apk.path)) }

    // apkanalyzer pierde el texto de los nodos (el dominio, los pines); aapt2 lo enseña, como T: 'texto'.
    private fun networkConfigTree(apk: File): String? = networkConfigPath(apk)?.let { path ->
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "build-tools/36.0.0/aapt2").path, "dump", "xmltree", "--file", path, apk.path).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().readText().also { check(process.waitFor() == 0) { it } }
    }

    // El pin de una clave: SHA-256 de su SubjectPublicKeyInfo, en Base64. Igual que el de openssl (teoría 03).
    private fun pin(spki: ByteArray) = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(spki))

    private fun certificatePin(pem: File) =
        pin(pem.inputStream().use { CertificateFactory.getInstance("X.509").generateCertificate(it) }.publicKey.encoded)

    private fun publicKeyPin(pem: File) =
        pin(Base64.getMimeDecoder().decode(pem.readLines().filterNot { it.startsWith("-----") }.joinToString("")))

    // De fuera solo se puede abrir la app desde el lanzador. Lo demás lo abre la propia app.
    @Test
    fun onlyTheLauncherIsExported() {
        val components = listOf("activity", "activity-alias", "service", "receiver", "provider").flatMap { manifest(release).children(it) }
        val exported = components.filter { it.getAttributeNS(android, "exported") == "true" }.map { it.getAttributeNS(android, "name") }
        assertEquals(listOf("dev.bitacora.alertas.MainActivity"), exported)
    }

    // Nada en claro: ni el atributo del manifiesto, ni la configuración de red, ni una URL http:// en el código.
    @Test
    fun cleartextIsForbidden() {
        val application = manifest(release).children("application").single()
        assertFalse(application.getAttributeNS(android, "usesCleartextTraffic") == "true", "usesCleartextTraffic")
        val base = assertNotNull(networkConfig(release), "No hay network_security_config").children("base-config").singleOrNull()
        assertEquals("false", base?.getAttribute("cleartextTrafficPermitted"))
        val dex = ZipFile(release).use { zip -> zip.getInputStream(zip.getEntry("classes.dex")).readBytes().toString(Charsets.ISO_8859_1) }
        assertFalse("http://alertas" in dex)
    }

    // El servidor de alertas, con dos pines (su clave y la de reserva, de server/) y una fecha de caducidad.
    @Test
    fun theServerIsPinned() {
        val tree = assertNotNull(networkConfigTree(release), "No hay network_security_config")
        val domain = tree.substringAfter("T: 'alertas.bitacora.example'", "").substringBefore("E: domain-config")
        assertTrue("E: pin-set" in domain, "No hay pin-set para alertas.bitacora.example")
        assertTrue("A: expiration=" in domain, "El pin-set no caduca")
        val pins = Regex("""E: pin \(.*\n\s+A: digest="SHA-256".*\n\s+T: '([^']+)'""").findAll(domain).map { it.groupValues[1] }.toSet()
        val expected = setOf(certificatePin(File("server/alertas.pem")), publicKeyPin(File("server/backup.pem")))
        assertEquals(expected, pins)
    }

    // Los certificados que instala la persona (un proxy para mirar el tráfico) solo valen en la debug.
    @Test
    fun userCertificatesOnlyInDebug() {
        val config = assertNotNull(networkConfig(debug), "No hay network_security_config")
        val user = config.children("certificates").filter { it.getAttribute("src") == "user" }
        assertTrue(user.isNotEmpty(), "La debug no confía en los certificados de la persona")
        assertTrue(user.all { (it.parentNode.parentNode as Element).tagName == "debug-overrides" })
        assertEquals("false", manifest(release).children("application").single().getAttributeNS(android, "debuggable").ifEmpty { "false" })
    }

    // La clave del servidor no está en el repositorio: llega de una propiedad de Gradle.
    @Test
    fun theKeyIsNotInTheRepository() {
        val leaks = File("app/src").walk().filter { it.isFile && "ak_live_" in it.readText() }.toList()
        assertEquals(emptyList(), leaks)
    }

    // Lint tiene comprobaciones de seguridad: ninguna salta.
    @Test
    fun lintFindsNoSecurityIssue() {
        val report = parse(File("app/build/reports/lint-results-release.xml").readText())
        val security = report.children("issue").filter { "Security" in it.getAttribute("category") }.map { it.getAttribute("id") }
        assertEquals(emptyList(), security)
    }
}

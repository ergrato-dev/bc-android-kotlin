import org.w3c.dom.Element
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Semana 38: lo que sale en el APK, mirado como lo mira quien lo descarga: el manifiesto y los XML compilados.
class SecurityTest {
    companion object {
        // Un solo build para los seis: la release y la debug de field.
        private val project by lazy { Project().also { it.run(":app:assembleFieldRelease", ":app:assembleFieldDebug") } }
        private val release by lazy { Apk(project.file("app/build/outputs/apk/field/release/app-field-release-unsigned.apk")) }
        private val debug by lazy { Apk(project.file("app/build/outputs/apk/field/debug/app-field-debug.apk")) }
        private const val ANDROID = "http://schemas.android.com/apk/res/android"
    }

    private fun parse(xml: String): Element =
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder().parse(xml.byteInputStream()).documentElement

    private fun Element.children(tag: String): List<Element> =
        getElementsByTagName(tag).let { list -> (0 until list.length).map { list.item(it) as Element } }

    private fun Element.android(name: String): String = getAttributeNS(ANDROID, name)

    private fun application(apk: Apk) = parse(apk.manifest()).children("application").single()

    // La ruta de un XML de res/xml en el APK (en la release, acortada: res/8G.xml), o null si no está.
    private fun path(apk: Apk, name: String): String? =
        runCatching { apk.analyze("resources", "value", "--type", "xml", "--config", "default", "--name", name) }.getOrNull()
            ?.lines()?.firstOrNull { it.startsWith("res/") }

    private fun xml(apk: Apk, name: String): Element? = path(apk, name)?.let { parse(apk.analyze("resources", "xml", "--file", it)) }

    // apkanalyzer pierde el texto de los nodos (los dominios, los pines); aapt2 lo enseña, como T: 'texto'.
    private fun tree(apk: Apk, name: String): String? = path(apk, name)?.let { path ->
        val home = System.getenv("ANDROID_HOME") ?: error("Define ANDROID_HOME con la carpeta del SDK de Android")
        val process = ProcessBuilder(File(home, "build-tools/36.0.0/aapt2").path, "dump", "xmltree", "--file", path, apk.file.path).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().readText().also { check(process.waitFor() == 0) { it } }
    }

    // El pin de una clave: SHA-256 de su SubjectPublicKeyInfo, en Base64. El mismo que da openssl (teoría 03).
    private fun pin(spki: ByteArray) = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(spki))

    // Nada en claro: ni con el atributo del manifiesto ni con la configuración de red.
    @Test
    fun cleartextIsForbidden() {
        assertFalse(application(release).android("usesCleartextTraffic") == "true")
        assertTrue(application(release).hasAttributeNS(ANDROID, "networkSecurityConfig"), "Sin networkSecurityConfig")
        val config = assertNotNull(xml(release, "network_security_config"))
        assertEquals("false", config.children("base-config").singleOrNull()?.getAttribute("cleartextTrafficPermitted"))
    }

    // El servidor de campo, con dos pines (su clave y la de reserva, de server/) y una fecha de caducidad.
    @Test
    fun theServerIsPinned() {
        val tree = assertNotNull(tree(release, "network_security_config"), "No hay network_security_config")
        val domain = tree.substringAfter("T: 'api.bitacora.example'", "").substringBefore("E: domain-config").substringBefore("E: debug-overrides")
        assertTrue("E: pin-set" in domain, "No hay pin-set para api.bitacora.example")
        assertTrue("A: expiration=" in domain, "El pin-set no caduca")
        val pins = Regex("""E: pin \(.*\n\s+A: digest="SHA-256".*\n\s+T: '([^']+)'""").findAll(domain).map { it.groupValues[1] }.toSet()
        val server = File("server/api.pem").inputStream().use { CertificateFactory.getInstance("X.509").generateCertificate(it) }
        val backup = Base64.getMimeDecoder().decode(File("server/backup.pem").readLines().filterNot { it.startsWith("-----") }.joinToString(""))
        assertEquals(setOf(pin(server.publicKey.encoded), pin(backup)), pins)
    }

    // Los certificados que instala la persona (un proxy) solo valen en la debug, y en ella saltan los pines.
    @Test
    fun userCertificatesOnlyInDebug() {
        val config = assertNotNull(xml(debug, "network_security_config"), "No hay network_security_config")
        val user = config.children("certificates").filter { it.getAttribute("src") == "user" }
        assertTrue(user.isNotEmpty(), "La debug no confía en los certificados de la persona")
        assertTrue(user.all { (it.parentNode.parentNode as Element).tagName == "debug-overrides" })
        assertTrue(user.all { it.getAttribute("overridePins") == "true" })
        assertFalse(application(release).android("debuggable") == "true")
    }

    // De fuera, sin permiso, solo se abre MainActivity: el lanzador y sus enlaces. Lo demás que se exporta (de las
    // bibliotecas) pide un permiso que una app normal no tiene.
    @Test
    fun onlyTheLauncherIsOpenToEveryone() {
        val components = listOf("activity", "activity-alias", "service", "receiver", "provider").flatMap { application(release).children(it) }
        val open = components.filter { it.android("exported") == "true" && it.android("permission").isEmpty() }.map { it.android("name") }
        assertEquals(listOf("dev.bitacora.MainActivity"), open)
    }

    // El token no va a las copias: ni a la de Google ni al pasar a otro móvil, ni en Android 11 o menos.
    @Test
    fun theTokenStaysOutOfBackups() {
        val rules = listOf("dataExtractionRules", "fullBackupContent").map { attribute ->
            assertTrue(application(release).hasAttributeNS(ANDROID, attribute), "Sin $attribute")
            assertNotNull(xml(release, if (attribute == "fullBackupContent") "backup_rules" else "data_extraction_rules"), attribute)
        }
        val excluded = rules.flatMap { it.children("exclude") }.filter { it.getAttribute("domain") == "file" && it.getAttribute("path") == "token.bin" }
        assertEquals(listOf("cloud-backup", "device-transfer", "full-backup-content"), excluded.map { (it.parentNode as Element).tagName }.sorted())
    }

    // bitacora://pair?token=… abre la app.
    @Test
    fun aLinkPairsThePhone() {
        val main = application(release).children("activity").single { it.android("name") == "dev.bitacora.MainActivity" }
        assertTrue(main.children("data").any { it.android("host") == "pair" }, "Ningún <data android:host=\"pair\">")
    }
}

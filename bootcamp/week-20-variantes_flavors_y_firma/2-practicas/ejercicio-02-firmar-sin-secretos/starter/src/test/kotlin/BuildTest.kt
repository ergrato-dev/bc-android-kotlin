import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val release = "app/build/outputs/apk/field/release"

    private fun Map<String, String>.asArguments() = map { (name, value) -> "-P$name=$value" }.toTypedArray()

    @Test
    fun withTheUploadKeyTheReleaseIsSigned() {
        val sandbox = Sandbox()
        sandbox.run("assembleFieldRelease", *Keystore.properties().asArguments())
        val signature = Apk(sandbox.file("$release/app-field-release.apk")).signature()
        assertContains(signature, "Signer #1 certificate DN: CN=Bitacora upload, O=Bootcamp, C=ES")
    }

    // Así lo hace un servidor de integración continua: los secretos le llegan como variables de entorno.
    @Test
    fun theKeyCanComeFromTheEnvironment() {
        val sandbox = Sandbox()
        sandbox.run("assembleFieldRelease", environment = Keystore.properties().mapKeys { "ORG_GRADLE_PROJECT_${it.key}" })
        val signature = Apk(sandbox.file("$release/app-field-release.apk")).signature()
        assertContains(signature, "Signer #1 certificate DN: CN=Bitacora upload, O=Bootcamp, C=ES")
    }

    @Test
    fun withoutTheKeyTheReleaseIsUnsigned() {
        val sandbox = Sandbox()
        sandbox.run("assembleFieldRelease")
        val signature = Apk(sandbox.file("$release/app-field-release-unsigned.apk")).signature()
        assertContains(signature, "DOES NOT VERIFY")
    }

    @Test
    fun aWrongPasswordStopsTheBuild() {
        val out = Sandbox().fail("assembleFieldRelease", *Keystore.properties(password = "wrong").asArguments()).output
        assertContains(out, "keystore password was incorrect")
    }

    @Test
    fun theDebugBuildKeepsTheDebugKey() {
        val sandbox = Sandbox()
        sandbox.run("assembleFieldDebug", *Keystore.properties().asArguments())
        val signature = Apk(sandbox.file("app/build/outputs/apk/field/debug/app-field-debug.apk")).signature()
        assertContains(signature, "Signer #1 certificate DN: C=US, O=Android, CN=Android Debug")
    }

    @Test
    fun theReportShowsWhichKeySignsEachVariant() {
        val out = Sandbox().run("signingReport", *Keystore.properties().asArguments()).output
        val fieldRelease = out.substringAfter("Variant: fieldRelease").substringBefore("----------")
        assertContains(fieldRelease, "Config: release")
        assertContains(fieldRelease, "Store: ${Keystore.file.path}")
    }

    @Test
    fun noPasswordOrKeyIsInTheProject() {
        val files = Sandbox().dir.walkTopDown().filter { it.isFile }.toList()
        val keys = files.filter { it.extension in setOf("jks", "keystore", "p12") }
        assertTrue(keys.isEmpty(), "Claves en el proyecto: $keys")
        val written = Regex("""(store|key)Password\s*=\s*"""")
        files.filter { it.extension in setOf("kts", "properties") }.forEach {
            assertFalse(written.containsMatchIn(it.readText()), "Contraseña escrita en ${it.name}")
        }
    }
}

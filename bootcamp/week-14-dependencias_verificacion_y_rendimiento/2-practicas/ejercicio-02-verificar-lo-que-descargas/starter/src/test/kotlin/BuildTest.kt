import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildTest {
    private val metadata = "gradle/verification-metadata.xml"

    // Cambia el sha256 del artefacto `artifact` por otro que no es el suyo, como si alguien lo hubiera sustituido.
    private fun Sandbox.tamper(artifact: String) {
        val file = file(metadata)
        val text = file.readText()
        val at = text.indexOf("<artifact name=\"$artifact\">")
        assertTrue(at >= 0, "$artifact no está en $metadata")
        val value = text.indexOf("value=\"", at) + "value=\"".length
        file.writeText(text.substring(0, value) + "0".repeat(64) + text.substring(value + 64))
    }

    @Test
    fun theMetadataUsesSha256() {
        val text = Sandbox().file(metadata).readText()
        assertTrue(text.contains("<sha256 "), text.take(500))
        assertFalse(text.contains("<sha1 ") || text.contains("<md5 "), "Solo sha256")
    }

    @Test
    fun theMetadataCoversThePlugins() {
        val text = Sandbox().file(metadata).readText()
        assertTrue(text.contains("name=\"kotlin-gradle-plugin\" version=\"2.4.21\""), "Faltan los artefactos del plugin de Kotlin")
    }

    @Test
    fun theBuildPassesWithCbor() {
        val sb = Sandbox()
        assertTrue(sb.file(metadata).readText().contains("name=\"kotlinx-serialization-cbor-jvm\" version=\"1.12.0\""))
        val out = sb.tree("runtimeClasspath")
        assertTrue(out.contains("org.jetbrains.kotlinx:kotlinx-serialization-cbor -> 1.12.0"), out)
        sb.run("build")
    }

    @Test
    fun aTamperedJarFailsTheBuild() {
        val sb = Sandbox()
        sb.tamper("kotlinx-serialization-json-jvm-1.12.0.jar")
        val out = sb.fail("compileKotlin").output
        assertTrue(out.contains("Dependency verification failed"), out)
        assertTrue(out.contains("kotlinx-serialization-json-jvm-1.12.0.jar"), out)
    }

    @Test
    fun aTamperedModuleFileFailsToo() {
        val sb = Sandbox()
        sb.tamper("kotlinx-serialization-json-jvm-1.12.0.module")
        val out = sb.fail("compileKotlin").output
        assertTrue(out.contains("kotlinx-serialization-json-jvm-1.12.0.module"), out)
    }

    @Test
    fun aTamperedPluginFailsBeforeConfiguring() {
        val sb = Sandbox()
        sb.tamper("kotlin-gradle-plugin-2.4.21-gradle96.jar")
        val out = sb.fail("help").output
        assertTrue(out.contains("Dependency verification failed for configuration"), out)
    }

    @Test
    fun anUnknownDependencyFailsTheBuild() {
        val sb = Sandbox()
        val build = sb.file("build.gradle.kts")
        build.writeText(build.readText().replace(
            "implementation(libs.kotlinx.serialization.json)",
            "implementation(libs.kotlinx.serialization.json)\n    implementation(\"org.jetbrains.kotlinx:kotlinx-serialization-protobuf\")",
        ))
        val out = sb.fail("compileKotlin").output
        assertTrue(out.contains("kotlinx-serialization-protobuf-jvm-1.12.0.jar"), out)
    }
}

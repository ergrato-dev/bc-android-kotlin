import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia el proyecto entero (sin build/ ni cachés) a una carpeta temporal y ejecuta Gradle dentro (TestKit).
// Cada test empieza con un proyecto limpio y puede cambiar archivos sin tocar el tuyo.
// Reutiliza ~/.gradle como carpeta de TestKit: el build de dentro no vuelve a descargar el plugin de Kotlin.
// Cada Project es "una máquina" con su propia caché local (`cache`), que se le pasa a Gradle con un init script: un
// test no ve los resultados de otro, ni los de tus builds. relocated() es el mismo proyecto en otra carpeta y con la
// misma caché; otherMachine(), otra carpeta y otra caché, vacía. `env` son las variables de entorno de esa máquina:
// CI, CACHE_USER y CACHE_PASSWORD no se heredan de la tuya.
class Project(private val source: String = ".", val cache: File = createTempDirectory("build-cache").toFile()) {
    val dir: File = createTempDirectory("bitacora").toFile()
    var env: Map<String, String> = emptyMap()
    private val init: File = File(cache.parentFile, "${cache.name}.init.gradle.kts")

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".idea", ".git")
        File(source).walkTopDown().onEnter { it.name !in skip }.filter { it.isFile }.forEach {
            it.copyTo(File(dir, it.relativeTo(File(source)).path))
        }
        init.writeText("settingsEvaluated {\n    buildCache {\n        local {\n            directory = file(\"${cache.invariantSeparatorsPath}\")\n        }\n    }\n}\n")
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively(); cache.deleteRecursively(); init.delete() })
    }

    fun relocated() = Project(source, cache)

    fun otherMachine() = Project(source)

    private fun runner(args: Array<out String>) = GradleRunner.create()
        .withProjectDir(dir)
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withArguments(*args, "--init-script", init.path, "--console=plain")
        .withEnvironment(System.getenv().filterKeys { it !in setOf("CI", "CACHE_USER", "CACHE_PASSWORD") } + env)

    fun run(vararg args: String): BuildResult = runner(args).build()

    fun fail(vararg args: String): BuildResult = runner(args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

fun BuildResult.executed(): List<String> = tasks.map { it.path }

// Ya está hecho: cambia en la copia el sha256 de un artefacto de gradle/verification-metadata.xml por otro que no es
// el suyo, como si alguien lo hubiera sustituido en el repositorio. `artifact` es el nombre del archivo:
// "kotlinx-serialization-json-jvm-1.12.0.jar", "gradle-kotlin-dsl-plugins-6.7.6.jar"...
fun Project.tamper(artifact: String) {
    val file = file("gradle/verification-metadata.xml")
    val text = file.readText()
    val at = text.indexOf("<artifact name=\"$artifact\">")
    check(at >= 0) { "$artifact no está en gradle/verification-metadata.xml" }
    val value = text.indexOf("value=\"", at) + "value=\"".length
    file.writeText(text.substring(0, value) + "0".repeat(64) + text.substring(value + 64))
}

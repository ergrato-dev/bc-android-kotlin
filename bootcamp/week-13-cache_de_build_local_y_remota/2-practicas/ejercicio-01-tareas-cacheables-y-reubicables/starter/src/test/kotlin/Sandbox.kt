import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia `sandbox/` (sin build/ ni cachés) a una carpeta temporal y ejecuta Gradle dentro (TestKit).
// Cada test tiene su propia caché de build local, en otra carpeta temporal (`cache`), que se le pasa a Gradle con un
// init script: así un test no ve los resultados de otro, y tu ~/.gradle no se llena.
// relocated() crea otra copia en otra carpeta que comparte la misma caché: es "el mismo proyecto en otro sitio".
class Sandbox(private val source: String = "sandbox", val cache: File = createTempDirectory("build-cache").toFile()) {
    val dir: File = createTempDirectory("sandbox").toFile()
    private val init: File = File(cache.parentFile, "${cache.name}.init.gradle.kts")

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".idea")
        File(source).walkTopDown().onEnter { it.name !in skip }.filter { it.isFile }.forEach {
            it.copyTo(File(dir, it.relativeTo(File(source)).path))
        }
        init.writeText("settingsEvaluated {\n    buildCache {\n        local {\n            directory = file(\"${cache.invariantSeparatorsPath}\")\n        }\n    }\n}\n")
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively(); cache.deleteRecursively(); init.delete() })
    }

    fun relocated() = Sandbox(source, cache)

    private fun runner(args: Array<out String>) = GradleRunner.create()
        .withProjectDir(dir)
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withArguments(*args, "--init-script", init.path, "--console=plain")

    fun run(vararg args: String): BuildResult = runner(args).build()

    fun fail(vararg args: String): BuildResult = runner(args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

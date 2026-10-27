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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia `sandbox/` (sin build/ ni cachés) a una carpeta temporal y ejecuta Gradle dentro (TestKit).
// Cada test empieza con un proyecto limpio y puede cambiar archivos sin tocar el tuyo.
// Reutiliza ~/.gradle como carpeta de TestKit: el build de dentro no vuelve a descargar el plugin de Kotlin.
class Sandbox(source: String = "sandbox") {
    val dir: File = createTempDirectory("sandbox").toFile()

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".idea")
        File(source).walkTopDown().onEnter { it.name !in skip }.filter { it.isFile }.forEach {
            it.copyTo(File(dir, it.relativeTo(File(source)).path))
        }
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
    }

    private fun runner(args: Array<out String>) = GradleRunner.create()
        .withProjectDir(dir)
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withArguments(*args, "--console=plain")

    fun run(vararg args: String): BuildResult = runner(args).build()

    fun fail(vararg args: String): BuildResult = runner(args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

fun BuildResult.executed(): List<String> = tasks.map { it.path }

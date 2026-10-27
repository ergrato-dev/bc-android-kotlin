import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia este proyecto entero (el plugin y consumer/, sin build/ ni cachés) a una carpeta temporal.
// run(...) ejecuta Gradle en el plugin; consumer(...), en consumer/. Sin withPluginClasspath: consumer encuentra el
// plugin solo si está publicado, como cualquier otro build. Reutiliza ~/.gradle como carpeta de TestKit.
class Workspace {
    val dir: File = createTempDirectory("workspace").toFile()

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".idea")
        File(".").walkTopDown().onEnter { it.name !in skip }.filter { it.isFile }.forEach {
            it.copyTo(File(dir, it.relativeTo(File(".")).path))
        }
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
    }

    private fun runner(projectDir: File, args: Array<out String>) = GradleRunner.create()
        .withProjectDir(projectDir)
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withArguments(*args, "--console=plain")

    fun run(vararg args: String): BuildResult = runner(dir, args).build()

    fun consumer(vararg args: String): BuildResult = runner(file("consumer"), args).build()

    fun consumerFails(vararg args: String): BuildResult = runner(file("consumer"), args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

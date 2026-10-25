import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia `sandbox/` a una carpeta temporal y ejecuta Gradle dentro (TestKit).
// Así cada test empieza con un proyecto limpio, sin `build/` ni cachés de una ejecución anterior.
class Sandbox(source: String = "sandbox") {
    val dir: File = createTempDirectory("sandbox").toFile()

    init {
        File(source).copyRecursively(dir)
        listOf("build", ".gradle", "local-cache").forEach { File(dir, it).deleteRecursively() }
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
    }

    private fun runner(args: Array<out String>) =
        GradleRunner.create().withProjectDir(dir).withArguments(*args, "--console=plain")

    fun run(vararg args: String): BuildResult = runner(args).build()

    fun fail(vararg args: String): BuildResult = runner(args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

fun BuildResult.executed(): List<String> = tasks.map { it.path }

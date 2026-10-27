import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: un build de prueba en una carpeta temporal que usa el plugin de este proyecto
// (withPluginClasspath: lo pone `java-gradle-plugin` con la tarea pluginUnderTestMetadata).
// Reutiliza ~/.gradle como carpeta de TestKit.
class TestBuild(script: String) {
    val dir: File = createTempDirectory("build-info").toFile()

    init {
        file("settings.gradle.kts").writeText("rootProject.name = \"demo\"\n")
        file("build.gradle.kts").writeText(script.trimIndent() + "\n")
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
    }

    private fun runner(args: Array<out String>) = GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withArguments(*args, "--console=plain")

    fun run(vararg args: String): BuildResult = runner(args).build()

    fun fail(vararg args: String): BuildResult = runner(args).buildAndFail()

    fun file(path: String) = File(dir, path)
}

fun BuildResult.outcome(path: String): TaskOutcome? = task(path)?.outcome

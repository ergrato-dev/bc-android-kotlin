import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia `sandbox/` (sin build/ ni cachés) a una carpeta temporal y ejecuta Gradle dentro (TestKit).
// AGP encuentra el SDK de Android con la variable de entorno ANDROID_HOME, que el build de dentro hereda de tu terminal.
class Sandbox(source: String = "sandbox") {
    val dir: File = createTempDirectory("sandbox").toFile()

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".idea", "local.properties")
        File(source).walkTopDown().onEnter { it.name !in skip }.filter { it.isFile && it.name !in skip }.forEach {
            it.copyTo(File(dir, it.relativeTo(File(source)).path))
        }
        Runtime.getRuntime().addShutdownHook(Thread { dir.deleteRecursively() })
    }

    // environment: variables de entorno de más para el build de dentro, además de las de tu terminal.
    private fun runner(args: Array<out String>, environment: Map<String, String>) = GradleRunner.create()
        .withProjectDir(dir)
        .withTestKitDir(File(System.getProperty("user.home"), ".gradle"))
        .withEnvironment(System.getenv() + environment)
        .withArguments(*args, "--console=plain")

    fun run(vararg args: String, environment: Map<String, String> = emptyMap()): BuildResult = runner(args, environment).build()

    fun fail(vararg args: String, environment: Map<String, String> = emptyMap()): BuildResult = runner(args, environment).buildAndFail()

    fun file(path: String) = File(dir, path)
}

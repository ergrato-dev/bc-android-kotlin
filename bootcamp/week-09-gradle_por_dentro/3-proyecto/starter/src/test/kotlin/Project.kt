import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

// Ya está hecho: copia el proyecto a una carpeta temporal (sin build/ ni cachés) y ejecuta Gradle dentro (TestKit).
// Así cada test empieza limpio y puede cambiar gradle.properties sin tocar tu proyecto de verdad.
// Reutiliza ~/.gradle como carpeta de TestKit: el build de dentro no vuelve a descargar el plugin de Kotlin.
class Project {
    val dir: File = createTempDirectory("bitacora").toFile()

    init {
        val skip = setOf("build", ".gradle", ".kotlin", ".git", ".idea")
        File(".").listFiles()!!.filter { it.name !in skip }.forEach { it.copyRecursively(File(dir, it.name)) }
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

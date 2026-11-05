import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

// Ya está hecho (semana 12): la tarea de la semana 9.
@CacheableTask
abstract class GenerateBuildInfo : DefaultTask() {
    @get:Input
    abstract val appVersion: Property<String>

    @get:Input
    abstract val domain: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("build-info.properties").writeText("version=${appVersion.get()}\ndomain=${domain.get()}\n")
    }
}

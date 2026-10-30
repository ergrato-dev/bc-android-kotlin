import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class BuildInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("buildInfo", BuildInfoExtension::class.java)
        extension.version.convention(project.provider { project.version.toString() })

        val task = project.tasks.register("generateBuildInfo", GenerateBuildInfo::class.java) {
            group = "bitacora"
            description = "Escribe build-info.properties con la versión y el dominio."
            appVersion.set(extension.version)
            domain.set(extension.domain)
            outputDir.set(project.layout.buildDirectory.dir("generated/build-info"))
        }

        // Paso 6: con com.android.application, la salida de la tarea va a los assets de cada variante (Variant API).
        project.pluginManager.withPlugin("java") {
            project.extensions.getByType(SourceSetContainer::class.java).named("main") {
                resources.srcDir(task.map { it.outputDir })
            }
        }
    }
}

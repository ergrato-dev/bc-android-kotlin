import com.android.build.api.variant.ApplicationAndroidComponentsExtension
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

        project.pluginManager.withPlugin("com.android.application") {
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java).onVariants { variant ->
                variant.sources.assets?.addGeneratedSourceDirectory(task, GenerateBuildInfo::outputDir)
            }
        }
        project.pluginManager.withPlugin("java") {
            project.extensions.getByType(SourceSetContainer::class.java).named("main") {
                resources.srcDir(task.map { it.outputDir })
            }
        }
    }
}

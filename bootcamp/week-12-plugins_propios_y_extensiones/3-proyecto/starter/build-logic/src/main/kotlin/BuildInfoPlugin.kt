import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Paso 2: tu plugin del ejercicio 01: la extensión `buildInfo` con la versión del proyecto por defecto,
        //         la tarea `generateBuildInfo` conectada con ella y, si hay `java`, su salida en los recursos de main.
    }
}

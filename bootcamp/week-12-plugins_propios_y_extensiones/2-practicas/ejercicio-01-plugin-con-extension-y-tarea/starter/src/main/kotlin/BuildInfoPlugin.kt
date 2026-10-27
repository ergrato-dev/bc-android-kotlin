import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Paso 2: crea la extensión `buildInfo` (project.extensions.create) y dale a `version` el valor por defecto
        //         del proyecto: version.convention(project.provider { project.version.toString() }). Nada de .get().
        // Paso 3: registra `generateBuildInfo` (tipo GenerateBuildInfo, group "bitacora", con description), conectando
        //         sus entradas con la extensión (appVersion.set(extension.version), ...) y su salida en
        //         build/generated/build-info.
        // Paso 4: solo si el proyecto aplica `java`: añade la salida de la tarea a los recursos de `main`, con
        //         project.pluginManager.withPlugin("java") { ... } y SourceSetContainer.
    }
}

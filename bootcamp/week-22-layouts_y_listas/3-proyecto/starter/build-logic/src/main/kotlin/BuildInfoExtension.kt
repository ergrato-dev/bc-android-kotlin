import org.gradle.api.provider.Property

// Ya está hecho (semana 12): lo que el usuario del plugin configura en su build con buildInfo { domain = "agro" }.
// Abstracta, como las tareas: Gradle crea la implementación de las propiedades.
abstract class BuildInfoExtension {
    abstract val domain: Property<String>
    abstract val version: Property<String>
}

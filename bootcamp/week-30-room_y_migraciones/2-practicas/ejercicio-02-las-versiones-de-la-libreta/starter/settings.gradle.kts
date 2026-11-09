// Ya está hecho: el plugin de Room está en el repositorio de Google, no en el portal de Gradle.
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "versiones"

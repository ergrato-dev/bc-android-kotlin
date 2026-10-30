// Ya está hecho: los plugins de Android están en el repositorio de Google (google()); el resto, en el Plugin Portal y
// en Maven Central. Las herramientas que AGP descarga al construir (aapt2) también salen de google().
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "bitacora"
include(":app")

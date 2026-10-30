// Ya está hecho: como en la semana 15. Robolectric y su android-all salen de Maven Central; AndroidX, de google().
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

rootProject.name = "cada-recurso-en-su-carpeta"
include(":app")

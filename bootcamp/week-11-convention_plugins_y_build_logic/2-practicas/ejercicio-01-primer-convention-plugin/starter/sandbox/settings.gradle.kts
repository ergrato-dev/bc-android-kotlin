// Paso 1: incluye build-logic como build de plugins, al principio del archivo:
//         pluginManagement { includeBuild("build-logic") }

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "bitacora"

include(":core", ":data", ":app")

rootProject.name = "bitacora"

// Paso 1: los repositorios, una sola vez, con dependencyResolutionManagement { ... }
//         (repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS y repositories { mavenCentral() }),
//         y los tres módulos con include(":core", ":data", ":app").

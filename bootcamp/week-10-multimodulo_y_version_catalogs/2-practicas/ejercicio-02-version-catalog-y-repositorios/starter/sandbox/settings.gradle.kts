rootProject.name = "bitacora"

// Paso 4: centraliza los repositorios aquí, con dependencyResolutionManagement { ... }:
//         repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS y repositories { mavenCentral() }.
//         Después borra los bloques repositories { } de los módulos.

include(":core", ":data", ":app")

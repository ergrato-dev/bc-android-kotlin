pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "bitacora"

include(":core", ":data", ":app")

// Paso 2: la caché remota, como en el ejercicio 02. La URL sale de la propiedad `bitacora.cacheUrl`; sin ella, solo
//         hay caché local. Solo el CI sube resultados (variable de entorno CI) y las credenciales salen de
//         CACHE_USER y CACHE_PASSWORD.

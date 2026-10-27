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

val cacheUrl = providers.gradleProperty("bitacora.cacheUrl").orNull

buildCache {
    if (cacheUrl != null) {
        remote<HttpBuildCache> {
            url = uri(cacheUrl)
            isPush = providers.environmentVariable("CI").isPresent
            credentials {
                username = providers.environmentVariable("CACHE_USER").orNull
                password = providers.environmentVariable("CACHE_PASSWORD").orNull
            }
        }
    }
}

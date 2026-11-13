pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "bitacora"

include(":core:model", ":core:database", ":core:data", ":feature:map", ":feature:notes", ":app")

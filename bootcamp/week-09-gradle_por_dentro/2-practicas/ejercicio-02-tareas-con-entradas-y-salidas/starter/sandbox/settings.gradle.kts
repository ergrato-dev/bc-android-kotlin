rootProject.name = "sandbox"

// La caché de resultados de este ejemplo vive dentro del proyecto (la usa el paso 5).
buildCache {
    local {
        directory = settingsDir.resolve("local-cache")
    }
}

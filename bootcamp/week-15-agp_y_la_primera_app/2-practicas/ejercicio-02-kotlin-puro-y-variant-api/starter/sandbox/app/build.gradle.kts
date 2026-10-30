plugins {
    alias(libs.plugins.android.application)
}

// Ya está hecho (ejercicio 01). La versión sale de gradle.properties.
android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = project.version.toString()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Paso 1: app usa core: implementation(project(":core")) en un bloque dependencies { }.

// Ya está hecho: la tarea de la semana 9, ahora para Android. Escribe build-info.properties en outputDir.
abstract class GenerateBuildInfo : DefaultTask() {
    @get:Input
    abstract val appVersion: Property<String>

    @get:Input
    abstract val domain: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun write() {
        outputDir.get().file("build-info.properties").asFile.writeText("version=${appVersion.get()}\ndomain=${domain.get()}\n")
    }
}

// Paso 2: registra la tarea generateBuildInfo con appVersion = la versión del proyecto y domain = "agro".
//         No le des outputDir: lo pone AGP en el paso 3.
// Paso 3: androidComponents { onVariants { variant -> ... } } y, dentro,
//         variant.sources.assets?.addGeneratedSourceDirectory(tarea, GenerateBuildInfo::outputDir).

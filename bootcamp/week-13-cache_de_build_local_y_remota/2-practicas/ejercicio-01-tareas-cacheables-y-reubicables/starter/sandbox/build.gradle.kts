plugins {
    kotlin("jvm") version "2.4.21"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

application {
    mainClass.set("MainKt")
}

// La tarea de la semana 9. Funciona y queda UP-TO-DATE, pero la caché de build no la guarda.
// Paso 2: márcala como cacheable con @CacheableTask.
// Paso 3: al hacerlo, Gradle exige saber cómo comparar el archivo de entrada: añade @get:PathSensitive(...) a `source`
//         con la opción que deja reutilizar el resultado desde otra carpeta.
abstract class CountObservations : DefaultTask() {
    @get:InputFile
    abstract val source: RegularFileProperty

    @get:Input
    abstract val skipHeader: Property<Boolean>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun count() {
        val lines = source.get().asFile.readLines().filter { it.isNotBlank() }
        val n = if (skipHeader.get()) maxOf(0, lines.size - 1) else lines.size
        report.get().asFile.writeText("observations=$n\n")
    }
}

tasks.register<CountObservations>("countObservations") {
    group = "bitacora"
    description = "Cuenta las observaciones del CSV."
    source = layout.projectDirectory.file("data/observations.csv")
    skipHeader = providers.gradleProperty("skipHeader").map { it.toBoolean() }.orElse(true)
    report = layout.buildDirectory.file("reports/observations.txt")
}

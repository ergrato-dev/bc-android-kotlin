plugins {
    kotlin("jvm") version "2.4.21"
    application
}

// ./gradlew run: el programa tal cual, sin R8.
application {
    mainClass = "dev.bitacora.keep.MainKt"
}

repositories {
    mavenCentral()
    // R8 se publica en el repositorio de Google, no en Maven Central.
    google()
}

// R8 como programa: la misma versión que usa AGP 9.4.1. No va en el classpath del programa ni de los tests.
val r8 = configurations.dependencyScope("r8")
val r8Classpath = configurations.resolvable("r8Classpath") { extendsFrom(r8.get()) }

dependencies {
    "r8"("com.android.tools:r8:9.4.24")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Ya está hecho: pasa el programa (tu código y la biblioteca de Kotlin) por R8, como hace AGP con la release, pero
// dejando clases de la JVM en vez de dex. Las reglas, de rules.pro; el mapping, a build/r8/mapping.txt.
val shrink = tasks.register<JavaExec>("shrink") {
    val program = files(tasks.jar, configurations.runtimeClasspath)
    val rules = layout.projectDirectory.file("rules.pro")
    val output = layout.buildDirectory.dir("r8")
    val jdk = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }.map { it.metadata.installationPath.asFile.path }
    inputs.files(program)
    inputs.file(rules)
    outputs.dir(output)
    classpath(r8Classpath)
    mainClass = "com.android.tools.r8.R8"
    argumentProviders += CommandLineArgumentProvider {
        val out = output.get().asFile.apply { mkdirs() }
        listOf("--classfile", "--release", "--lib", jdk.get(), "--pg-conf", rules.asFile.path,
            "--pg-map-output", "$out/mapping.txt", "--output", "$out/shrunk.jar") + program.files.map { it.path }
    }
}

// ./gradlew runShrunk: el programa que sale de R8.
tasks.register<JavaExec>("runShrunk") {
    dependsOn(shrink)
    classpath(layout.buildDirectory.file("r8/shrunk.jar"))
    mainClass = "dev.bitacora.keep.MainKt"
}

// Lo que sale de R8 es una entrada de los tests: si cambia rules.pro, se vuelven a ejecutar.
tasks.test {
    inputs.files(shrink).withPropertyName("shrunk")
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

// Ya está hecho: core es Kotlin puro, sin nada de Android. Se compila y se prueba en la JVM, en milisegundos.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.launcher)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}

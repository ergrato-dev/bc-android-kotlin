// El proyecto raíz es solo el banco de pruebas: aplica Kotlin para ejecutar los tests de TestKit (src/test) sobre
// una copia del proyecto entero. Los módulos aplican el mismo plugin con alias(libs.plugins.kotlin.jvm).
plugins {
    alias(libs.plugins.kotlin.jvm)
    // Paso 3: declara aquí el plugin de serialization para los módulos, sin aplicarlo:
    //         alias(libs.plugins.kotlin.serialization) apply false
}

// Paso 1 (continuación): este bloque sobra cuando los repositorios estén en settings.gradle.kts; bórralo.
repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
    testImplementation(gradleTestKit())
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

// Paso 6: cambia el bloque plugins por tu convention plugin y borra lo que ya pone. Lo propio del módulo se queda.
plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
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

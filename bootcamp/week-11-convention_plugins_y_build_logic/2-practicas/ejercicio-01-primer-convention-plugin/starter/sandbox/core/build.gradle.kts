// Paso 4: cambia alias(libs.plugins.kotlin.jvm) por id("bitacora.kotlin-base") y borra kotlin { } y tasks.test { }:
//         ya los pone el convention plugin. Lo demás se queda.
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

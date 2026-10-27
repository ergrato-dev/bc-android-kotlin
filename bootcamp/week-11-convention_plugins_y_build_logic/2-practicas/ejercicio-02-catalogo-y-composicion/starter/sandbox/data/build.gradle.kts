// Paso 3: id("bitacora.kotlin-library") en lugar de la base y `java-library`; fuera las dependencias de prueba.
//         Lo propio de data (serialization y api(project(":core"))) se queda.
plugins {
    id("bitacora.kotlin-base")
    alias(libs.plugins.kotlin.serialization)
    `java-library`
}

dependencies {
    api(project(":core"))
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}

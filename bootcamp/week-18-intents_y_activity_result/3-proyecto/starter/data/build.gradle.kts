plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
}

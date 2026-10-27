// Paso 3: core solo aplica id("bitacora.kotlin-library"). Todo lo demás lo pone el convention plugin.
plugins {
    id("bitacora.kotlin-base")
    `java-library`
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}

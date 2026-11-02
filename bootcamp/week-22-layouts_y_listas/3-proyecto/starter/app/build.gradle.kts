plugins {
    id("bitacora.android-application")
    alias(libs.plugins.kotlin.compose)
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
    }
    // demo enseña la Bitácora con observaciones de ejemplo, sin salir al campo; field es la de verdad, y la que miran
    // lint y check.
    flavorDimensions += "environment"
    productFlavors {
        create("demo") {
            dimension = "environment"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
        create("field") {
            dimension = "environment"
            isDefault = true
        }
    }
    // Solo los idiomas de la app: los demás textos de AndroidX no van al APK.
    androidResources {
        localeFilters += listOf("es", "en")
    }
}

// La demo no se publica.
androidComponents {
    beforeVariants(selector().withFlavor("environment" to "demo").withBuildType("release")) { it.enable = false }
}

dependencies {
    implementation(project(":data"))
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    // El BOM pone las versiones de Compose. ui-test-manifest es la actividad vacía de createComposeRule; va en debug
    // porque los tests de la JVM usan el manifiesto de la variante.
    implementation(platform(libs.compose.bom))
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

buildInfo {
    domain = "agro"
}

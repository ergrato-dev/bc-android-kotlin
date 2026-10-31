plugins {
    id("bitacora.android-application")
    // Paso 1: el plugin de Compose.
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
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel.savedstate)
    // Paso 1: las bibliotecas de Compose: activity-compose, material3, lifecycle-viewmodel-compose y
    // lifecycle-runtime-compose. Con activity-compose, activity sobra.
    // Ya está hecho: el BOM, que pone las versiones de Compose, y lo que necesitan los tests. ui-test-manifest es la
    // actividad vacía de createComposeRule; va en debug porque los tests de la JVM usan el manifiesto de la variante.
    implementation(platform(libs.compose.bom))
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

buildInfo {
    domain = "agro"
}

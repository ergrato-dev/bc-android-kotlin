plugins {
    id("bitacora.android-application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        // Paso 2: la clave de Maps, de la propiedad mapsApiKey (manifestPlaceholders), vacía si no está.
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
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    // Paso 1: adaptive-navigation3, maps-compose y play-services-maps (y fragment, si lint lo pide).
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

// Observation y las demás clases de core no pasan por el compilador de Compose: para él son inestables. Este archivo le
// dice que son estables (todas sus propiedades son val de tipos estables), y compara con equals al saltarse una fila.
// Con -PcomposeReports, los informes de estabilidad en app/build/compose.
composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose-stability.conf"))
    if (providers.gradleProperty("composeReports").isPresent) {
        reportsDestination = layout.buildDirectory.dir("compose")
        metricsDestination = layout.buildDirectory.dir("compose")
    }
}

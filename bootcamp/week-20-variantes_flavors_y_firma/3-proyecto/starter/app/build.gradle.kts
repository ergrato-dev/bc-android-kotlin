plugins {
    id("bitacora.android-application")
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
    }
    // Paso 3: la dimensión "environment" y sus flavors, demo y field. field, por defecto.
    // Solo los idiomas de la app: los demás textos de AndroidX no van al APK.
    androidResources {
        localeFilters += listOf("es", "en")
    }
}

// Paso 4: sin demoRelease.

dependencies {
    implementation(project(":data"))
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel.savedstate)
}

buildInfo {
    domain = "agro"
}

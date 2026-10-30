plugins {
    id("bitacora.android-application")
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
    }
    // Solo los idiomas de la app: los demás textos de AndroidX no van al APK.
    androidResources {
        localeFilters += listOf("es", "en")
    }
}

dependencies {
    implementation(project(":data"))
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel.savedstate)
}

buildInfo {
    domain = "agro"
}

plugins {
    id("bitacora.android-application")
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
    }
    // Paso 6: androidResources { localeFilters += ... } con los idiomas de tu app, regiones incluidas.
}

dependencies {
    implementation(project(":data"))
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel.savedstate)
}

buildInfo {
    domain = "agro"
}

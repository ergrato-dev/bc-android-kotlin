plugins {
    id("bitacora.android-application")
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
    }
}

dependencies {
    implementation(project(":data"))
    // Paso 4: activity y lifecycle-viewmodel-savedstate.
}

buildInfo {
    domain = "agro"
}

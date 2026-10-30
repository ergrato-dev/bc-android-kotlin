plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Ya está hecho: la app usa el SDK de analítica de un proveedor (el módulo :sdk, que hace de biblioteca de terceros).
dependencies {
    implementation(libs.activity)
    implementation(project(":sdk"))
}

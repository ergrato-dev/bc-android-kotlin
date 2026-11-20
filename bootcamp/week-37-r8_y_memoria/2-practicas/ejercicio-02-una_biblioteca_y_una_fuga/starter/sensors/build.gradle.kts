plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.bitacora.sensors"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
        // TODO (paso 3): las reglas que viajan en el AAR.
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

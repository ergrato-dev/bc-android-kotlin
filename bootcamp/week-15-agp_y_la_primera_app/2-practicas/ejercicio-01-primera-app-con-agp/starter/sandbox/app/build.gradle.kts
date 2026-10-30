plugins {
    alias(libs.plugins.android.application)
}

// Paso 1: el bloque android { } con namespace "dev.bitacora" y compileSdk 37.
// Paso 2: dentro, defaultConfig { } con applicationId "dev.bitacora.agro", minSdk 26, targetSdk 36,
//         versionCode 1 y versionName "1.0.0".

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

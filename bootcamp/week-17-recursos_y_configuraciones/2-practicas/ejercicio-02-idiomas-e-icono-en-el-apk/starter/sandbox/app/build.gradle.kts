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
    // Paso 1: androidResources { } con localeFilters: los tres idiomas de la app, "en", "es" y "b+es+419".
    // Paso 2: en el mismo bloque, generateLocaleConfig = true. Necesita app/src/main/res/resources.properties.
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Ya está hecho: AndroidX trae sus propios textos traducidos a decenas de idiomas.
dependencies {
    implementation(libs.activity)
}

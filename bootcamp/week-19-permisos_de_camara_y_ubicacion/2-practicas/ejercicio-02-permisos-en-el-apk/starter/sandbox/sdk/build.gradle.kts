// Ya está hecho: hace de SDK de un tercero. No lo toques: en un proyecto real, no es tu código.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.example.sdk"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
    }
}

kotlin {
    jvmToolchain(21)
}

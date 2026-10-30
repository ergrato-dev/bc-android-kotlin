plugins {
    id("com.android.application")
}

android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = project.version.toString()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Paso 3: Robolectric para todas las apps: testOptions (recursos y --add-opens), el bundle robolectric en
//         testImplementation, una restricción que sube guava (GHSA-xxph-c9ww-hj94) y android-all resuelto por Gradle en
//         modo offline (teoría 05).

pluginManager.apply(BuildInfoPlugin::class.java)

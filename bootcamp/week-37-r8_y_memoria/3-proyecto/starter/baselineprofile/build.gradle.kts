// Semana 36: los benchmarks y el generador de Baseline Profiles. Un módulo de test (com.android.test): su APK lleva solo
// los tests, y se instala junto a la app de :app para manejarla desde fuera, como una persona, con UiAutomator.
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "dev.bitacora.baselineprofile"
    compileSdk = 37
    defaultConfig {
        // BaselineProfileRule necesita API 28 o más (sin root, 33 o más).
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // La app tiene flavors y este módulo no: se mide la de verdad.
        missingDimensionStrategy("environment", "field")
    }
    targetProjectPath = ":app"
    // Un emulador con los servicios de Google (systemImageSource = "google"), que Gradle crea, arranca y apaga solo.
    testOptions.managedDevices.localDevices.create("pixel6Api36") {
        device = "Pixel 6"
        apiLevel = 36
        systemImageSource = "google"
        testedAbi = "x86_64"
    }
}

// ./gradlew :app:generateBaselineProfile genera el perfil en el emulador gestionado, no en lo que haya conectado.
baselineProfile {
    managedDevices += "pixel6Api36"
    useConnectedDevices = false
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.uiautomator)
    implementation(libs.benchmark.macro.junit4)
    // benchmark-traceprocessor 1.5.0 trae wire-runtime 6.4.0 (GHSA-9rm7-3qhh-h2mc); corregida en 6.4.5. Solo va en el APK
    // de los tests, no en la app, pero se queda en la corregida, como guava.
    constraints {
        implementation(libs.wire.runtime)
    }
}

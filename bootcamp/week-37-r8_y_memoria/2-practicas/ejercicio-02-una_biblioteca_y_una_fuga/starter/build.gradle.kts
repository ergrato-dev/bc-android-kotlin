// Ya está hecho: AGP y los plugins se cargan una vez, en el raíz. El raíz aplica Kotlin para sus tests (src/test), que
// miran lo que sale del build: el AAR de la biblioteca, los APK y el mapping.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.launcher)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Antes de los tests, lo que miran: el AAR, los dos APK de la app y el de sus tests del dispositivo.
tasks.test {
    dependsOn(":sensors:assembleRelease", ":app:assembleRelease", ":app:assembleDebug", ":app:assembleDebugAndroidTest")
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

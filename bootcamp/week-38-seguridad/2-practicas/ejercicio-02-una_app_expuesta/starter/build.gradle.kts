// Ya está hecho. El raíz aplica Kotlin para sus tests (src/test), que miran lo que sale del build: los dos APK y el
// informe de lint de la release.
plugins {
    alias(libs.plugins.android.application) apply false
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

tasks.test {
    dependsOn(":app:assembleRelease", ":app:assembleDebug", ":app:lintRelease")
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

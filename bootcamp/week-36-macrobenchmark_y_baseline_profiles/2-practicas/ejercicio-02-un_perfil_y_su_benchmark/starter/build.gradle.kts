// Ya está hecho: AGP y los plugins se cargan una vez, en el raíz. El raíz aplica Kotlin para sus tests (src/test), que
// miran lo que sale del build: el perfil, los APK y sus manifiestos.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.baselineprofile) apply false
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

// Antes de los tests, los APK que miran: el de publicar y, si ya existe el módulo :benchmark, el suyo y el de la app
// que mide.
tasks.test {
    dependsOn(":app:assembleRelease")
    dependsOn(provider {
        if (findProject(":benchmark") == null) emptyList()
        else listOf(":benchmark:assembleBenchmarkRelease", ":app:assembleBenchmarkRelease")
    })
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

// Ya está hecho: el ViewModel en la JVM. viewModelScope necesita Dispatchers.Main, que en la JVM no existe: los tests
// ponen uno de pruebas (teoría 05).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

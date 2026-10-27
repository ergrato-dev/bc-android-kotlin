plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)

    // Paso 3: una restricción (constraints { }) que sube org.jetbrains:annotations a la versión del catálogo,
    //         con because("...") explicando por qué.
}

// Paso 4: org.jetbrains:annotations solo hace falta para compilar. Quítala del classpath de ejecución con
//         configurations.runtimeClasspath { exclude(...) }.

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

application {
    mainClass.set("MainKt")
}

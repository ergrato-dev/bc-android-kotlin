plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

// Ya está hecho (ejercicio 01): la restricción de annotations y su exclusión del classpath de ejecución.
// Paso 2: añade kotlinx-serialization-cbor (del BOM de serialization; va al catálogo) y actualiza la verificación.
dependencies {
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)

    constraints {
        implementation(libs.jetbrains.annotations) {
            because("el equipo usa en todos los módulos la versión del catálogo")
        }
    }
}

configurations.runtimeClasspath {
    exclude(group = "org.jetbrains", module = "annotations")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

application {
    mainClass.set("MainKt")
}

// Paso 6: cambia el bloque plugins por tu convention plugin y borra lo que ya pone. Lo propio del módulo se queda.
plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":data"))
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

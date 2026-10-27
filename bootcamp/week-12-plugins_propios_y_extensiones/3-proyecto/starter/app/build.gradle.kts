plugins {
    id("bitacora.kotlin-application")
}

dependencies {
    implementation(project(":data"))
}

application {
    mainClass.set("MainKt")
}

// Paso 5 (continuación): el dominio de tu Bitácora: buildInfo { domain = "<tu dominio>" }.

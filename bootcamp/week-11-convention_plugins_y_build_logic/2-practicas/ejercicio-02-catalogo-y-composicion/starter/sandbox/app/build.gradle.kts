// Paso 3: id("bitacora.kotlin-application") en lugar de la base y application. El mainClass se queda.
plugins {
    id("bitacora.kotlin-base")
    application
}

dependencies {
    implementation(project(":data"))
}

application {
    mainClass.set("MainKt")
}

// Paso 7: app pasa a ser una aplicación Android con bitacora.android-application: applicationId con tu dominio,
//         AndroidManifest.xml, res/values/strings.xml, MainActivity en vez de Main.kt, y paquetes en core y data.
plugins {
    id("bitacora.kotlin-application")
}

dependencies {
    implementation(project(":data"))
}

application {
    mainClass.set("MainKt")
}

buildInfo {
    domain = "agro"
}

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

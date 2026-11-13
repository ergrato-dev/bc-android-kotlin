plugins {
    id("bitacora.module")
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:map"))
    implementation(project(":feature:notes"))
}

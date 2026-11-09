plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    // Flow sale en la interfaz del repositorio: quien la use también la necesita.
    api(libs.coroutines.core)
    // Las preferencias de la lista. -core es la parte sin Android: se prueba en la JVM con un archivo temporal.
    implementation(libs.datastore.preferences.core)
    testImplementation(libs.coroutines.test)
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
}

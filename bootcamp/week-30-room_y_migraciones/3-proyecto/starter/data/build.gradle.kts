plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

// El esquema de cada versión, en JSON: se sube al repositorio, y las migraciones y sus tests salen de ahí.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(project(":core"))
    // Flow sale en la interfaz del repositorio: quien la use también la necesita.
    api(libs.coroutines.core)
    // Las preferencias de la lista. -core es la parte sin Android: se prueba en la JVM con un archivo temporal.
    implementation(libs.datastore.preferences.core)
    // Room 3 es multiplataforma: en la JVM, room3-runtime-jvm; en la app, room3-runtime-android. api: la app crea la
    // base de datos con el builder de Android. KSP genera BitacoraDatabase_Impl y ObservationDao_Impl, en Kotlin.
    api(libs.room.runtime)
    ksp(libs.room.compiler)
    // El compilador de Room trae guava 33.2.1-jre (GHSA-xxph-c9ww-hj94): corre en el build, no va en el APK, pero se
    // queda en la corregida, como el de Dagger en app.
    constraints {
        ksp(libs.guava)
    }
    // En la JVM no hay SQLite del sistema: los tests traen el suyo. En el móvil, Room usa el de Android.
    testImplementation(libs.sqlite.bundled)
    testImplementation(libs.coroutines.test)
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
}

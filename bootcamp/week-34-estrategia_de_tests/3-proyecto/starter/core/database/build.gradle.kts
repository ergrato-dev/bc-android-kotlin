plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

// El esquema de cada versión, en JSON: se sube al repositorio, y las migraciones y sus tests salen de ahí.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Level sale en la entidad.
    api(project(":core:model"))
    // Flow sale en el DAO.
    api(libs.coroutines.core)
    // Room 3 es multiplataforma: en la JVM, room3-runtime-jvm; en la app, room3-runtime-android. api: RoomDatabase y
    // su builder salen en las firmas. KSP genera BitacoraDatabase_Impl y ObservationDao_Impl, en Kotlin.
    api(libs.room.runtime)
    ksp(libs.room.compiler)
    // El compilador de Room trae guava 33.2.1-jre (GHSA-xxph-c9ww-hj94): corre en el build, no va en el APK, pero se
    // queda en la corregida, como el de Dagger en app.
    constraints {
        ksp(libs.guava)
    }
    // En la JVM no hay SQLite del sistema: los tests traen el suyo. En el móvil, Room usa el de Android.
    testImplementation(libs.sqlite.bundled)
    // MigrationTestHelper, para probar la migración de la versión 1 a la 2.
    testImplementation(libs.room.testing)
    testImplementation(libs.coroutines.test)
}

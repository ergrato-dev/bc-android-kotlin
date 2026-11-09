// Ya está hecho: Room en la JVM, sin Android. KSP genera el código de la base de datos y de los DAO; el plugin de Room
// guarda el esquema de cada versión en schemas/. El driver empaquetado trae su propio SQLite, el mismo en todas partes.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    // El compilador de Room trae guava 33.2.1-jre (GHSA-xxph-c9ww-hj94). Corre en el build, no en la app, pero se
    // queda en la corregida.
    constraints {
        ksp(libs.guava)
    }
    implementation(libs.sqlite.bundled)
    implementation(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

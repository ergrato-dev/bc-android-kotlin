// El proyecto raíz es solo el banco de pruebas: aplica Kotlin para ejecutar los tests de TestKit (src/test) sobre
// una copia del proyecto entero. No usa los convention plugins a propósito: así el plugin de Kotlin se carga aquí,
// una vez, y un cambio en build-logic no cambia el classpath de compileKotlin de los módulos (semana 11, teoría 04).
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
    testImplementation(gradleTestKit())
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Paso 3: los tests de src/test ejecutan Gradle sobre una copia del proyecto, pero Gradle no lo sabe: para él, las
//         entradas de esta tarea son las clases de test y su classpath. Declara también los archivos del proyecto
//         como entradas (inputs.files, rutas relativas), o un cambio en core/ dejará esta tarea UP-TO-DATE.
tasks.test {
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

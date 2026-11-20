// El proyecto raíz es solo el banco de pruebas: aplica Kotlin para ejecutar los tests de TestKit (src/test) sobre
// una copia del proyecto entero. No usa los convention plugins a propósito: así el plugin de Kotlin se carga aquí,
// una vez, y un cambio en build-logic no cambia el classpath de compileKotlin de los módulos (semana 11, teoría 04).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    // Semana 34: el informe de cobertura de los módulos core, junto.
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":core:model"))
    kover(project(":core:database"))
    kover(project(":core:network"))
    kover(project(":core:data"))
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

tasks.test {
    inputs.files(fileTree(layout.projectDirectory) { exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**", ".git/**", ".idea/**") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("bitacoraFiles")
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

kover {
    reports {
        // Lo que escribe Room (BitacoraDatabase_Impl, ObservationDao_Impl) no lo escribimos nosotros: no cuenta.
        filters {
            excludes {
                classes("*_Impl", "*_Impl$*")
            }
        }
        // ./gradlew koverVerify (y check) falla si la cobertura de líneas de los módulos core baja del 90%.
        verify {
            rule {
                minBound(90)
            }
        }
    }
    currentProject {
        // Los tests del raíz construyen copias del proyecto con TestKit: no prueban clases de aquí.
        instrumentation {
            disabledForTestTasks.add("test")
        }
    }
}

// El proyecto raíz es solo el banco de pruebas: aplica Kotlin para ejecutar los tests de TestKit (src/test) sobre
// una copia del proyecto entero. No usa los convention plugins a propósito: así el plugin de Kotlin se carga aquí,
// una vez, y un cambio en build-logic no cambia el classpath de compileKotlin de los módulos (semana 11, teoría 04).
plugins {
    alias(libs.plugins.android.application) apply false
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

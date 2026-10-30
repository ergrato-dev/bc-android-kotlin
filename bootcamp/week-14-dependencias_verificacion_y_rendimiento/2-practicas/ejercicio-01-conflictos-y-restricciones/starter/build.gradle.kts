plugins {
    kotlin("jvm") version "2.4.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    // Los tests construyen sandbox/: si cambia, se vuelven a ejecutar aunque los tests no hayan cambiado.
    inputs.files(fileTree("sandbox") { exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("sandbox")
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

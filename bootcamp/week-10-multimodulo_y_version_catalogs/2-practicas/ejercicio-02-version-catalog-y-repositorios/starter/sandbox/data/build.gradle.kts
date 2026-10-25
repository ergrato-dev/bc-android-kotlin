// Paso 3: igual que core. Aquí además serialization: libs.plugins.kotlin.serialization, platform(libs.serialization.bom)
//         y libs.serialization.json.
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.12.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}

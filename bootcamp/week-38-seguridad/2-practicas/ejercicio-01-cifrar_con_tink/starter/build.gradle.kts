plugins {
    kotlin("jvm") version "2.4.21"
}

repositories {
    mavenCentral()
}

dependencies {
    // Tink para la JVM. En Android es com.google.crypto.tink:tink-android, con la misma API y AndroidKeysetManager.
    implementation("com.google.crypto.tink:tink:1.24.0")
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
    testLogging {
        events("passed", "failed")
    }
}

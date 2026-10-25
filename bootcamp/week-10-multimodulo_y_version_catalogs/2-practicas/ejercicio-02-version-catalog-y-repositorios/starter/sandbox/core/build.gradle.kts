// Paso 3: este módulo ya funciona con versiones escritas a mano. Pásalo al catálogo: plugins con alias(libs.plugins...),
//         platform(libs.junit.bom), libs.bundles.testing, y quita repositories (paso 4).
// Ya está hecho: copia su estructura para los otros dos módulos.
plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
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

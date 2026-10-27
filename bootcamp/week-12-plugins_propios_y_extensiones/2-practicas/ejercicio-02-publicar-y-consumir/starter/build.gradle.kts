// Ya está hecho (ejercicio 01): el build del plugin. Paso 1: publícalo.
plugins {
    `kotlin-dsl`
    // Paso 1: añade `maven-publish`.
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        register("buildInfo") {
            id = "bitacora.build-info"
            implementationClass = "BuildInfoPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

// Paso 1 (continuación): las coordenadas, group = "dev.bitacora" y version = "1.0.0", y un repositorio de destino
//         en la carpeta build/repo: publishing { repositories { maven { name = "local"; url = uri(...) } } }.

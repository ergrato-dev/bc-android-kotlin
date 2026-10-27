// Un build cuyo producto es un plugin. `kotlin-dsl` trae Kotlin, la API de Gradle y `java-gradle-plugin`
// (el bloque gradlePlugin { } y la tarea validatePlugins).
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Paso 1: registra el plugin: gradlePlugin { plugins { register("buildInfo") { id = "bitacora.build-info";
//         implementationClass = "BuildInfoPlugin" } } }

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

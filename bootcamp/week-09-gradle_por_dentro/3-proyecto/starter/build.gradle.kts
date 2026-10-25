plugins {
    kotlin("jvm") version "2.4.21"
    application
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

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}

// Desde aquí, tu trabajo (pasos 1 a 6 del README del proyecto).
//
// Paso 1: lee `bitacora.version` y `bitacora.domain` con providers.gradleProperty (nunca con `project.property`
//         dentro de una acción). Si falta alguna, el build falla al configurar con el mensaje exacto
//         "Falta la propiedad bitacora.version en gradle.properties" (o ...bitacora.domain...). Pon la versión
//         en `version`.
// Paso 2: una clase de tarea `GenerateBuildInfo` (abstracta, DefaultTask) con dos entradas (@Input: appVersion y
//         domain) y una salida (@OutputDirectory: outputDir). Su acción escribe en outputDir el archivo
//         build-info.properties con dos líneas, `version=<v>` y `domain=<d>`, cada una con salto de línea.
// Paso 3: regístrala como `generateBuildInfo` (group "bitacora", con description), con la salida en
//         build/generated/build-info, y añade esa carpeta a los recursos de `main` con
//         sourceSets.main { resources.srcDir(<la tarea>.map { it.outputDir }) } para que `run` la encuentre.
// Paso 4: no escribas nada aquí: desde Gradle 9 el jar y el zip son reproducibles por defecto. Lo que sí haces es
//         comprobarlo con un test (paso 7 del README del proyecto).
// Paso 5: una tarea `dist` (tipo Zip, group "bitacora", con description) que mete el jar, build-info.properties y
//         README.md en build/dist/bitacora-<version>.zip, sin carpetas intermedias.
// Paso 6: una tarea `printVersion` (group "bitacora") que imprime "bitacora <version>". Tiene que ser compatible
//         con la caché de configuración: nada de `project` dentro de doLast.

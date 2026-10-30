plugins {
    alias(libs.plugins.android.application)
}

// Ya está hecho: la app de la semana 16, con Robolectric.
android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        // Robolectric necesita leer internals del JDK 21 (semana 16, teoría 05).
        unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.activity)
    testImplementation(libs.bundles.robolectric)
    constraints {
        testImplementation(libs.guava) {
            because("GHSA-xxph-c9ww-hj94: Robolectric 4.17 trae guava 33.6.0-jre, afectada; corregida en 33.7.2")
        }
    }
}

// Ya está hecho: Robolectric ejecuta los tests sobre un Android entero (android-all, 204 MB). Por defecto lo descarga él,
// fuera de Gradle, a ~/.m2. Así lo resuelve Gradle, como cualquier dependencia, y Robolectric no sale a la red
// (semana 16, teoría 05).
val androidAll = configurations.dependencyScope("androidAll")
val androidAllJars = configurations.resolvable("androidAllJars") { extendsFrom(androidAll.get()) }
dependencies { androidAll(libs.android.all) }

tasks.withType<Test>().configureEach {
    val jars = files(androidAllJars)
    inputs.files(jars).withNormalizer(ClasspathNormalizer::class.java).withPropertyName("androidAll")
    systemProperty("robolectric.offline", "true")
    jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Drobolectric.dependency.dir=${jars.singleFile.parentFile}") }
    testLogging {
        events("passed", "failed")
    }
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.bitacora.alertas"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.bitacora.alertas"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    // Lint no para el build por un aviso: los de seguridad los mira ExposureTest en su informe.
    lint {
        abortOnError = false
    }
    // Ya está hecho: Robolectric lee el manifiesto (y con él targetSdk), y en JDK 21 necesita entrar en jdk.internal.
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

// Ya está hecho: Robolectric sin red, con el android-all del catálogo (como la Bitácora desde la semana 34).
val androidAll = configurations.dependencyScope("androidAll")
val androidAllJars = configurations.resolvable("androidAllJars") { extendsFrom(androidAll.get()) }

dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    "androidAll"(libs.android.all)
    constraints {
        testImplementation(libs.guava) {
            because("GHSA-xxph-c9ww-hj94: Robolectric 4.17 trae guava 33.6.0-jre, afectada; corregida en 33.7.2")
        }
    }
}

tasks.withType<Test>().configureEach {
    val jars = files(androidAllJars)
    inputs.files(jars).withNormalizer(ClasspathNormalizer::class.java).withPropertyName("androidAll")
    systemProperty("robolectric.offline", "true")
    jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Drobolectric.dependency.dir=${jars.singleFile.parentFile}") }
}

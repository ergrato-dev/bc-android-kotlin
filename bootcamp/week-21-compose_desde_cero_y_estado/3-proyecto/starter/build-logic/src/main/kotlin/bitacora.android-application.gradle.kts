plugins {
    id("com.android.application")
}

android {
    namespace = "dev.bitacora"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = project.version.toString()
    }
    // Toda app de la Bitácora declara sus idiomas: el usuario puede elegir el de la app en Android 13 o superior.
    androidResources {
        generateLocaleConfig = true
    }
    // La clave de subida no está en el repositorio: sus datos llegan como propiedades de Gradle, de
    // ~/.gradle/gradle.properties, de -P o de variables de entorno ORG_GRADLE_PROJECT_*. Sin ellas, el release sale
    // sin firmar, y el build sigue funcionando en cualquier máquina.
    val storeFile = providers.gradleProperty("bitacoraStoreFile")
    signingConfigs {
        create("release") {
            this.storeFile = storeFile.orNull?.let(::file)
            storePassword = providers.gradleProperty("bitacoraStorePassword").orNull
            keyAlias = providers.gradleProperty("bitacoraKeyAlias").orNull
            keyPassword = providers.gradleProperty("bitacoraKeyPassword").orNull
        }
    }
    // La de debug se instala al lado de la publicada: otro applicationId.
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            if (storeFile.isPresent) signingConfig = signingConfigs.getByName("release")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

val libs = versionCatalogs.named("libs")
fun library(name: String) = libs.findLibrary(name).orElseThrow { GradleException("El catálogo no tiene la biblioteca $name") }

val androidAll = configurations.dependencyScope("androidAll")
val androidAllJars = configurations.resolvable("androidAllJars") { extendsFrom(androidAll.get()) }

dependencies {
    "testImplementation"(libs.findBundle("robolectric").orElseThrow { GradleException("El catálogo no tiene el bundle robolectric") })
    "androidAll"(library("android-all"))
    constraints {
        "testImplementation"(library("guava")) {
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

pluginManager.apply(BuildInfoPlugin::class.java)

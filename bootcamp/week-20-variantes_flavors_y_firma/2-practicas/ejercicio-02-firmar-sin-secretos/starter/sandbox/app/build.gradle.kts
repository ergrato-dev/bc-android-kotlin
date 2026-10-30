plugins {
    alias(libs.plugins.android.application)
}

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
    // Paso 1: así no. Cualquiera que clone el repositorio tiene las contraseñas, y la clave tendría que estar en el
    // proyecto. Léelas de las propiedades de Gradle uploadStoreFile, uploadStorePassword, uploadKeyAlias y
    // uploadKeyPassword.
    signingConfigs {
        create("release") {
            storeFile = file("upload.jks")
            storePassword = "bitacora123"
            keyAlias = "upload"
            keyPassword = "bitacora123"
        }
    }
    // Ya está hecho: los tipos de build y los flavors del ejercicio 01.
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        // Paso 2: sin la clave, el release sale sin firmar.
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("demo") {
            dimension = "environment"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
        create("field") {
            dimension = "environment"
            isDefault = true
        }
    }
}

// Ya está hecho: la demo no se publica.
androidComponents {
    beforeVariants(selector().withFlavor("environment" to "demo").withBuildType("release")) { it.enable = false }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.activity)
}

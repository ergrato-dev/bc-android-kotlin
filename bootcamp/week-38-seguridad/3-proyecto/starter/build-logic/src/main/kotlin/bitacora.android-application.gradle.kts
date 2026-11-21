plugins {
    id("com.android.application")
    id("bitacora.android-base")
}

android {
    namespace = "dev.bitacora"
    defaultConfig {
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (storeFile.isPresent) signingConfig = signingConfigs.getByName("release")
        }
    }
}

pluginManager.apply(BuildInfoPlugin::class.java)

plugins {
    id("bitacora.android-application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.roborazzi)
}

android {
    defaultConfig {
        applicationId = "dev.bitacora.agro"
        // La clave de Maps, de la propiedad mapsApiKey (~/.gradle/gradle.properties), vacía si no está: nunca en el repositorio.
        manifestPlaceholders["mapsApiKey"] = providers.gradleProperty("mapsApiKey").getOrElse("")
    }
    // demo enseña la Bitácora con observaciones de ejemplo, sin salir al campo; field es la de verdad, y la que miran
    // lint y check.
    flavorDimensions += "environment"
    productFlavors {
        create("demo") {
            dimension = "environment"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            buildConfigField("String", "API_URL", "\"https://demo.bitacora.example/v1/\"")
        }
        create("field") {
            dimension = "environment"
            isDefault = true
            buildConfigField("String", "API_URL", "\"https://api.bitacora.example/v1/\"")
        }
    }
    // En AGP 9, BuildConfig está apagada: la URL del servidor de cada flavor va ahí.
    buildFeatures {
        buildConfig = true
    }
    // Solo los idiomas de la app: los demás textos de AndroidX no van al APK.
    androidResources {
        localeFilters += listOf("es", "en")
    }
    // Semana 35: los tests instrumentados (src/androidTest) arrancan la app con HiltTestApplication. El orquestador
    // ejecuta cada test en su propio proceso y, con clearPackageData, borra los datos de la app entre uno y otro.
    // pixel6Api36 es un emulador que Gradle crea, arranca y apaga: ./gradlew pixel6Api36FieldDebugAndroidTest.
    // Necesita aceleración por hardware (KVM en Linux).
    defaultConfig {
        testInstrumentationRunner = "dev.bitacora.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        managedDevices {
            localDevices {
                create("pixel6Api36") {
                    device = "Pixel 6"
                    apiLevel = 36
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

// La demo no se publica.
androidComponents {
    beforeVariants(selector().withFlavor("environment" to "demo").withBuildType("release")) { it.enable = false }
}

dependencies {
    implementation(project(":core:data"))
    // El motor de la red en el móvil. data solo conoce HttpClient.
    implementation(libs.ktor.client.okhttp)
    // Semana 32: la sincronización en segundo plano, y en los tests, el WorkManager que se maneja a mano.
    implementation(libs.work.runtime)
    testImplementation(libs.work.testing)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    // El compilador de Dagger trae guava 33.6.0-jre (GHSA-xxph-c9ww-hj94). Corre en el build, no va en el APK, pero
    // con la misma restricción que los tests (bitacora.android-application) se queda en la corregida. Semana 35: también
    // el de los tests instrumentados.
    constraints {
        ksp(libs.guava)
        kspTest(libs.guava)
        kspAndroidTest(libs.guava)
    }
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.adaptive.navigation3)
    // Semana 33: el mapa, en su módulo, con sus dependencias de Maps.
    implementation(project(":feature:map"))
    // play-services-maps (que llega con :feature:map) declara fragment 1.0.0: con registerForActivityResult, lint pide
    // 1.3.0 o más.
    implementation(libs.fragment)
    // El BOM pone las versiones de Compose. ui-test-manifest es la actividad vacía de createComposeRule; va en debug
    // porque los tests de la JVM usan el manifiesto de la variante.
    implementation(platform(libs.compose.bom))
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.coroutines.test)
    // Semana 34: FakeRepository y FakeSettings, de los testFixtures de :core:data; y Turbine para los Flow.
    testImplementation(testFixtures(project(":core:data")))
    testImplementation(libs.turbine)
    // Semana 35: capturas con Roborazzi sobre Robolectric, y Espresso-Intents para lo que sale de la app.
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.espresso.intents)
    debugImplementation(libs.compose.ui.test.manifest)
    // Semana 35: los tests que corren en un dispositivo, con Hilt y los fakes de :core:data.
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(testFixtures(project(":core:data")))
    kspAndroidTest(libs.hilt.compiler)
    androidTestUtil(libs.androidx.test.orchestrator)
}

buildInfo {
    domain = "agro"
}

// Observation y las demás clases de core no pasan por el compilador de Compose: para él son inestables. Este archivo le
// dice que son estables (todas sus propiedades son val de tipos estables), y compara con equals al saltarse una fila.
// Con -PcomposeReports, los informes de estabilidad en app/build/compose.
composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose-stability.conf"))
    if (providers.gradleProperty("composeReports").isPresent) {
        reportsDestination = layout.buildDirectory.dir("compose")
        metricsDestination = layout.buildDirectory.dir("compose")
    }
}

// Semana 35: las capturas de referencia, con el código y en el repositorio. Con roborazzi.test.verify=true
// (gradle.properties), ./gradlew test compara con ellas; ./gradlew :app:recordRoborazziFieldDebug las rehace.
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
}

// build no compila los tests instrumentados: sin esto, un androidTest roto solo se ve con un dispositivo.
tasks.named("check") {
    dependsOn("assembleFieldDebugAndroidTest")
}

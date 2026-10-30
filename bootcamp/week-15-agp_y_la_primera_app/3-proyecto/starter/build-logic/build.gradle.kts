plugins {
    `kotlin-dsl`
}

// Paso 4: build-logic necesita AGP para compilar bitacora.android-application (y google() en su settings).
dependencies {
    implementation(libs.kotlin.gradle.plugin)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
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
}

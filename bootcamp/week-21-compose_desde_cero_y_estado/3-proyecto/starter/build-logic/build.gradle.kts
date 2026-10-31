plugins {
    `kotlin-dsl`
}

// build-logic necesita AGP para compilar bitacora.android-application (semana 15).
dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
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

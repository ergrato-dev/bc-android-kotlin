plugins {
    `kotlin-dsl`
}

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

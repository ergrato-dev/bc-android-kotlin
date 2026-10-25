// Paso 4: el módulo core: plugins alias(libs.plugins.kotlin.jvm) y `java-library`, testImplementation de
//         platform(libs.junit.bom) y libs.bundles.testing, kotlin { jvmToolchain(21); allWarningsAsErrors } y
//         tasks.test { useJUnitPlatform() }. Sin repositories: ya están en settings. No depende de nadie.

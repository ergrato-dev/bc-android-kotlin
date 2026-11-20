plugins {
    id("bitacora.android-library")
    alias(libs.plugins.kotlin.compose)
}

android {
    // Su propio paquete y su propia R: dev.bitacora.map.R. Con el de la app, los manifiestos chocan al juntarse.
    namespace = "dev.bitacora.map"
}

dependencies {
    // Observation sale en la firma de MapScreen.
    api(project(":core:model"))
    // Maps es cosa del mapa: va en el APK, pero la app no lo ve al compilar.
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    // La actividad vacía de createComposeRule, en el manifiesto de debug de la biblioteca.
    debugImplementation(libs.compose.ui.test.manifest)
}

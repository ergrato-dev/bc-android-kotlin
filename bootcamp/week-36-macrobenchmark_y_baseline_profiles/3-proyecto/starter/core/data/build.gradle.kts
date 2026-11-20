plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
    // Semana 34: los fakes que comparten los tests de otros módulos (src/testFixtures).
    `java-test-fixtures`
}

dependencies {
    // Las firmas públicas deciden: BitacoraDatabase y su builder (bitacora()), ObservationApi y NetworkError
    // (RefreshResult) y Observation salen en ellas. Quien usa el repositorio los necesita al compilar.
    api(project(":core:model"))
    api(project(":core:database"))
    api(project(":core:network"))
    // Las preferencias de la lista. -core es la parte sin Android: se prueba en la JVM con un archivo temporal. Solo
    // dentro: la app ve SettingsRepository, no DataStore.
    implementation(libs.datastore.preferences.core)
    // El JSON de la semana 29, que se importa la primera vez.
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
    testImplementation(libs.sqlite.bundled)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.turbine)
}

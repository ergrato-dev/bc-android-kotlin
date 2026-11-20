plugins {
    id("bitacora.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // La red, sin motor: el cliente se crea con el que le pasen. api: HttpClient y HttpClientEngine salen en
    // bitacoraClient. En la app, OkHttp; en los tests, MockEngine, que contesta sin salir del proceso.
    api(libs.ktor.client.core)
    // El JSON es cosa de dentro: nadie fuera de este módulo ve ContentNegotiation ni Json.
    implementation(platform(libs.serialization.bom))
    implementation(libs.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

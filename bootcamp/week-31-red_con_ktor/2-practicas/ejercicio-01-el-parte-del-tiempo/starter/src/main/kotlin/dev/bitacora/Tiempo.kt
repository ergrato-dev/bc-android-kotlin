package dev.bitacora

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.Serializable

// El parte del tiempo de una finca, tal como lo manda el servidor (https://api.tiempo.example/v2/forecast).
// Paso 2: los nombres del servidor son temp_c, rain_mm y wind_kmh (@SerialName), y wind puede no venir.
@Serializable
data class Hour(
    val time: String,
    val temperature: Double,
    val rain: Double,
    val wind: Double?,
)

@Serializable
data class Forecast(val place: String, val hourly: List<Hour>)

// Lo que se manda al avisar de una helada, y lo que contesta el servidor.
// Paso 2: en el servidor, minTemperature se llama min_temp_c.
@Serializable
data class Report(val place: String, val minTemperature: Double)

@Serializable
data class Created(val id: Long)

const val BASE_URL = "https://api.tiempo.example/v2/"

// La clave no está en el código: la pone quien crea el cliente (en la app, desde fuera del repositorio).
// Paso 3: expectSuccess, ContentNegotiation con JSON que ignora los campos que no conoce, y defaultRequest con
// BASE_URL y la cabecera X-Api-Key.
@Suppress("UNUSED_PARAMETER")
fun forecastClient(engine: HttpClientEngine, apiKey: String): HttpClient = HttpClient(engine)

class ForecastApi(private val client: HttpClient) {
    // Paso 4: lat, lon y hours como parámetros de la URL.
    @Suppress("UNUSED_PARAMETER")
    suspend fun forecast(latitude: Double, longitude: Double, hours: Int): Forecast = client.get("forecast").body()

    // Paso 5: el aviso, en el cuerpo y en JSON.
    @Suppress("UNUSED_PARAMETER")
    suspend fun report(report: Report): Long = client.post("reports").body<Created>().id
}

// Paso 6: las horas con al menos `threshold` mm de lluvia: las que no sirven para fumigar.
@Suppress("UNUSED_PARAMETER")
fun Forecast.rainyHours(threshold: Double = 0.5): List<String> = emptyList()

// Paso 6: la mínima del parte; sin horas, null.
fun Forecast.minTemperature(): Double? = null

package dev.bitacora

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Station(val id: String, val temperature: Double)

// Lo que contesta una llamada a la red, en palabras de la app: un valor o uno de los problemas que se saben tratar.
sealed interface Answer<out T> {
    data class Ok<T>(val value: T) : Answer<T>
    data object Offline : Answer<Nothing>
    data object Timeout : Answer<Nothing>
    data class Http(val status: Int) : Answer<Nothing>
    data object Unreadable : Answer<Nothing>
}

const val TIMEOUT = 10_000L
const val RETRIES = 2

// Paso 3: HttpTimeout con TIMEOUT para la petición entera.
// Paso 4: HttpRequestRetry: los 5xx, RETRIES veces, con espera exponencial y sin la parte al azar
// (randomizationMs = 0), para que los tests sepan cuándo llega cada intento.
fun stationClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    defaultRequest { url("https://api.tiempo.example/v2/") }
}

// Paso 2: de la excepción a la respuesta. Un catch por problema: el tiempo agotado (HttpRequestTimeoutException), sin
// red (IOException), un código de error (ResponseException) y una respuesta que no se puede leer
// (ContentConvertException). Lo demás, y la cancelación, sigue subiendo.
suspend fun <T> safely(block: suspend () -> T): Answer<T> = Answer.Ok(block())

class StationApi(private val client: HttpClient) {
    suspend fun station(id: String): Answer<Station> = safely { client.get("stations/$id").body<Station>() }
}

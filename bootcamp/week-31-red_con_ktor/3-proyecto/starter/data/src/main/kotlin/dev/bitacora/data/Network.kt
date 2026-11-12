package dev.bitacora.data

import dev.bitacora.core.Observation
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.Serializable

// Lo que manda el servidor, con sus nombres: la respuesta es suya, no nuestra. Ni la entidad ni el dominio cambian si
// el servidor cambia un nombre.
// Paso 2: en el servidor, la posición es lat y lng (@SerialName), y puede no venir.
@Serializable
data class RemoteObservation(
    val code: String,
    val level: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
)

@Serializable
data class ObservationsResponse(val observations: List<RemoteObservation>)

// Lo que puede salir mal, en palabras de la app: la pantalla elige el mensaje sin saber de Ktor ni de OkHttp.
enum class NetworkError { OFFLINE, TIMEOUT, SERVER, BAD_RESPONSE }

sealed interface RefreshResult {
    data class Done(val received: Int) : RefreshResult
    data class Failed(val error: NetworkError) : RefreshResult
}

const val CONNECT_TIMEOUT = 5_000L
const val REQUEST_TIMEOUT = 15_000L

// Un cliente para toda la app: el motor (OkHttp en el móvil, MockEngine en los tests) y la URL base, de fuera.
// Paso 3: expectSuccess, ContentNegotiation con JSON que ignora los campos que no conoce, HttpTimeout con
// CONNECT_TIMEOUT y REQUEST_TIMEOUT, y defaultRequest con la URL base.
@Suppress("UNUSED_PARAMETER")
fun bitacoraClient(engine: HttpClientEngine, baseUrl: String): HttpClient = HttpClient(engine)

class ObservationApi(private val client: HttpClient) {
    // Paso 4: GET de "observations" (sin / delante), leído como ObservationsResponse y traducido a Observation. Un
    // nivel que la app no conoce se salta.
    suspend fun observations(): List<Observation> = emptyList()
}

// Paso 5: de la excepción al error de la app. Los tiempos agotados (HttpRequestTimeoutException,
// ConnectTimeoutException, SocketTimeoutException) también son IOException: van antes. ResponseException es un código
// de error; ContentConvertException y NoTransformationFoundException, una respuesta que no se puede leer. null: no es de
// la red (un fallo nuestro, o la cancelación), y sigue subiendo.
fun Throwable.toNetworkError(): NetworkError? = null

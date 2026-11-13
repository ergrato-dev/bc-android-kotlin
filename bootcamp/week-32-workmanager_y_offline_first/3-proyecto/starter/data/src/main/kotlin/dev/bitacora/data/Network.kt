package dev.bitacora.data

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

// Lo que manda el servidor, con sus nombres: la respuesta es suya, no nuestra. Ni la entidad ni el dominio cambian si
// el servidor cambia un nombre.
@Serializable
data class RemoteObservation(
    val code: String,
    val level: String,
    val description: String,
    @SerialName("lat") val latitude: Double? = null,
    @SerialName("lng") val longitude: Double? = null,
    // Paso 4: updatedAt, que el servidor llama updated_at. Un servidor antiguo no lo manda.
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
// La URL base acaba en /, y las rutas no empiezan por /: si no, "v1" se pierde.
fun bitacoraClient(engine: HttpClientEngine, baseUrl: String): HttpClient = HttpClient(engine) {
    // Un 404 o un 503 es una excepción, no una respuesta que se intenta leer como lista.
    expectSuccess = true
    // Un campo nuevo en el servidor no rompe la app que ya está instalada.
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    // Sin esto, una petición puede esperar para siempre.
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT
        requestTimeoutMillis = REQUEST_TIMEOUT
    }
    defaultRequest { url(baseUrl) }
}

// Lo que dice el servidor, con sus clases: traducirlo a la tabla es cosa del repositorio (semana 32).
// Paso 4: put(observation) y delete(code, updatedAt).
class ObservationApi(private val client: HttpClient) {
    suspend fun observations(): List<RemoteObservation> = client.get("observations").body<ObservationsResponse>().observations
}

// De la excepción al error de la app. Los tiempos agotados también son IOException: van antes. null: no es de la red
// (un fallo nuestro, o la cancelación), y sigue subiendo.
fun Throwable.toNetworkError(): NetworkError? = when (this) {
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> NetworkError.TIMEOUT
    is IOException -> NetworkError.OFFLINE
    is ResponseException -> NetworkError.SERVER
    is ContentConvertException, is NoTransformationFoundException -> NetworkError.BAD_RESPONSE
    else -> null
}

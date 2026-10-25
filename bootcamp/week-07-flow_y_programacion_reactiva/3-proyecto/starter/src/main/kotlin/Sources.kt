import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

// Base de la semana 6. Si ya tienes tus versiones, úsalas en lugar de este archivo.
fun interface ObservationSource {
    suspend fun fetch(): List<String>
}

sealed interface FetchResult {
    data class Fetched(val source: String, val lines: List<String>) : FetchResult
    data class Failed(val source: String, val reason: String) : FetchResult
}

// Pide UNA fuente con tiempo límite. Tiempo agotado -> Failed("tiempo agotado (<timeoutMs> ms)");
// excepción -> Failed(e.message ?: nombre de la clase); la cancelación se propaga.
suspend fun fetchOne(name: String, source: ObservationSource, timeoutMs: Long): FetchResult =
    try {
        withTimeoutOrNull(timeoutMs) { source.fetch() }
            ?.let { FetchResult.Fetched(name, it) }
            ?: FetchResult.Failed(name, "tiempo agotado ($timeoutMs ms)")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        FetchResult.Failed(name, e.message ?: e::class.simpleName ?: "error")
    }

// Para tus pruebas: una fuente que tarda ms y devuelve lines.
fun slowSource(ms: Long, vararg lines: String): ObservationSource = ObservationSource {
    delay(ms)
    lines.toList()
}

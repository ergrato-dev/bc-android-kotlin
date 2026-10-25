import kotlinx.coroutines.delay

// Una fuente remota (simulada): entrega líneas "CODE;descripción;cantidad;medida". Puede tardar y puede fallar.
fun interface ObservationSource {
    suspend fun fetch(): List<String>
}

sealed interface FetchResult {
    data class Fetched(val source: String, val lines: List<String>) : FetchResult
    data class Failed(val source: String, val reason: String) : FetchResult
}

data class SyncResult(
    val logbook: Logbook,
    val invalid: List<ParseResult.Invalid>,
    val failed: List<FetchResult.Failed>,
    val duplicates: Int,
)

// Paso 1: pide TODAS las fuentes a la vez, pero nunca más de maxParallel al mismo tiempo (Semaphore).
// - require(timeoutMs > 0) y require(maxParallel > 0).
// - El resultado va en el orden del mapa, no en el orden en que terminan.
// - El tiempo límite cuenta desde que la fuente EMPIEZA (no mientras espera su turno).
//   Si se agota -> Failed(source, "tiempo agotado (<timeoutMs> ms)").
// - Si la fuente lanza una excepción -> Failed(source, e.message ?: nombre simple de la clase).
// - Si CANCELAN la sincronización entera, la cancelación se propaga: no la conviertas en un Failed.
// El fallo de una fuente no cancela a las demás.
suspend fun fetchSources(
    sources: Map<String, ObservationSource>,
    timeoutMs: Long,
    maxParallel: Int,
): List<FetchResult> = TODO("Paso 1")

// Paso 2: sync junta todo en una bitácora.
// - Cada línea pasa por parseLine. Válida -> logbook.add (si add devuelve false cuenta como duplicada).
//   Inválida -> invalid, SIN cambiar la línea ni el motivo.
// - Se procesa en el orden de las fuentes y, dentro de cada una, en el orden de sus líneas.
// - Las fuentes que fallaron van a failed, en orden.
suspend fun sync(
    domain: String,
    sources: Map<String, ObservationSource>,
    timeoutMs: Long = 2_000,
    maxParallel: Int = 3,
): SyncResult = TODO("Paso 2")

// Para tus pruebas: una fuente que tarda ms y devuelve lines.
fun slowSource(ms: Long, vararg lines: String): ObservationSource = ObservationSource {
    delay(ms)
    lines.toList()
}

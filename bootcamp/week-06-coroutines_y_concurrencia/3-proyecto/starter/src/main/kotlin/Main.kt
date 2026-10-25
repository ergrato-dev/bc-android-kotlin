import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

// Paso 3: sustituye las fuentes por datos de tu dominio. Una tiene que ir lenta, otra fallar y otra repetir una línea.
fun main() = runBlocking {
    val sources = mapOf(
        "norte" to slowSource(300, "OBS-001;Ejemplo uno;3;12.5", "OBS-002;Ejemplo dos;1;57.25"),
        "sur" to slowSource(200, "OBS-001;Ejemplo uno;3;12.5", "OBS-003;;4;1.0"),
        "este" to ObservationSource { delay(100); error("sin cobertura") },
        "oeste" to slowSource(5_000, "OBS-009;Tarde;1;1.0"),
    )
    lateinit var result: SyncResult
    val ms = measureTimeMillis { result = sync("MI-DOMINIO", sources, timeoutMs = 1_000, maxParallel = 3) }
    println(result.logbook.report())
    println(result.logbook.countByLevel())
    result.invalid.forEach { println("Descartada: ${it.line} -> ${it.reason}") }
    result.failed.forEach { println("Fuente caída: ${it.source} -> ${it.reason}") }
    println("Duplicadas: ${result.duplicates}. Tiempo total: $ms ms")
}

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

// Paso 6: sustituye las fuentes por datos de tu dominio. Una lenta, una que falle, una con una línea repetida y
// una línea inválida. Imprime cada evento NADA MÁS llegar y, a la vez, cómo cambian los contadores por nivel.
fun main() = runBlocking {
    val store = LogbookStore("MI-DOMINIO")
    val sources = mapOf(
        "norte" to slowSource(300, "OBS-001;Ejemplo uno;3;12.5", "OBS-002;Ejemplo dos;1;57.25"),
        "sur" to slowSource(200, "OBS-001;Ejemplo uno;3;12.5", "OBS-003;;4;1.0"),
        "este" to ObservationSource { delay(100); error("sin cobertura") },
    )
    val watcher = store.countByLevel.onEach { println("niveles: $it") }.launchIn(this)
    val summary = syncInto(store, sources, timeoutMs = 1_000, maxParallel = 3)
        .onEach { println("evento: $it") }
        .summarize()
    println(summary)
    watcher.cancel()
}

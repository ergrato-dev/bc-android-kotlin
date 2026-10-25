import kotlinx.coroutines.flow.Flow

// Lo que va pasando mientras se sincroniza, en el orden en que cada fuente TERMINA (no el del mapa).
sealed interface SyncEvent {
    data class Imported(
        val source: String,
        val added: Int,
        val duplicates: Int,
        val invalid: List<ParseResult.Invalid>,
    ) : SyncEvent

    data class SourceFailed(val source: String, val reason: String) : SyncEvent
}

data class SyncSummary(val added: Int, val duplicates: Int, val invalid: Int, val failedSources: List<String>)

// Paso 4: pide todas las fuentes a la vez (nunca más de maxParallel) y va EMITIENDO un evento por fuente en cuanto
// termina. Usa channelFlow: una coroutine por fuente, un Semaphore, y fetchOne para cada una.
// - Fetched -> cada línea por parseLine; válidas a store.add (false = duplicada); inválidas se guardan.
//   Emite Imported(source, added, duplicates, invalid) con las inválidas en el orden de las líneas.
// - Failed -> SourceFailed(source, reason).
// - require(timeoutMs > 0) y require(maxParallel > 0), comprobados al RECOGER el flujo.
// - Si el colector deja de recoger (por ejemplo con take(1)), las fuentes pendientes se CANCELAN.
fun syncInto(
    store: LogbookStore,
    sources: Map<String, ObservationSource>,
    timeoutMs: Long = 2_000,
    maxParallel: Int = 3,
): Flow<SyncEvent> = TODO("Paso 4")

// Paso 5: recoge todos los eventos y los resume (fold): total añadidas, duplicadas, inválidas y los nombres de las
// fuentes caídas, en el orden en que llegaron.
suspend fun Flow<SyncEvent>.summarize(): SyncSummary = TODO("Paso 5")

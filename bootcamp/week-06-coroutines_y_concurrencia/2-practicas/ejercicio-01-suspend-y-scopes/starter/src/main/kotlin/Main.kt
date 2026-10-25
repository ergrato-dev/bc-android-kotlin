import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// Paso 1: fetchAll lanza TODAS las llamadas a la vez y devuelve los resultados en el orden de ids.
// Tres llamadas de 100 ms tienen que tardar 100 ms en total, no 300. Usa coroutineScope, async y awaitAll.
suspend fun <T> fetchAll(ids: List<Int>, fetch: suspend (Int) -> T): List<T> = TODO("Paso 1")

// Paso 2: race ejecuta a y b a la vez, devuelve el resultado de la que termine primero y CANCELA a la otra.
// Pista: un CompletableDeferred recibe el primer resultado; al terminar, cancela los hijos del scope.
suspend fun <T> race(a: suspend () -> T, b: suspend () -> T): T = TODO("Paso 2")

// Paso 3: retry llama a block hasta times veces. Si todas fallan, lanza el ÚLTIMO error.
// Una CancellationException NO es un fallo que reintentar: se relanza al instante.
suspend fun <T> retry(times: Int, block: suspend (attempt: Int) -> T): T = TODO("Paso 3")

// Paso 4: si block no termina en timeoutMs, devuelve default (withTimeoutOrNull, no withTimeout).
suspend fun <T> orDefault(timeoutMs: Long, default: T, block: suspend () -> T): T = TODO("Paso 4")

// Paso 5: lanza una cuenta atrás en este scope y devuelve su Job. Emite from, from-1, ..., 1 con stepMs entre cada
// uno; si el Job se cancela, deja de emitir.
fun CoroutineScope.startCountdown(from: Int, stepMs: Long, onTick: (Int) -> Unit): Job = TODO("Paso 5")

fun main() = runBlocking {
    println(fetchAll(listOf(1, 2, 3)) { delay(100); it * 10 })
    println(race({ delay(300); "lenta" }, { delay(100); "rápida" }))
    println(orDefault(50, "tarde") { delay(500); "a tiempo" })
}

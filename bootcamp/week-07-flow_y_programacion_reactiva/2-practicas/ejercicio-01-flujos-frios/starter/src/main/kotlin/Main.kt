import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

// Paso 1: un flujo frío que emite 1, 2, ..., count con periodMs de espera ANTES de cada valor.
// Frío = no hace nada hasta que alguien lo recoge, y empieza de cero con cada recogida.
fun ticker(count: Int, periodMs: Long): Flow<Int> = TODO("Paso 1")

// Paso 2: la suma acumulada. [1, 2, 3, 4] -> [1, 3, 6, 10]. Un operador intermedio escrito como extensión de Flow.
fun Flow<Int>.runningSum(): Flow<Int> = TODO("Paso 2")

// Paso 3: el primer valor mayor que limit, o null si no hay ninguno. Un operador terminal: en cuanto lo
// encuentra, el flujo de origen se CANCELA (no sigue emitiendo).
suspend fun firstAbove(source: Flow<Int>, limit: Int): Int? = TODO("Paso 3")

// Paso 4: el estado de una pantalla. asUiState emite Loading al empezar, Success(valor) por cada valor y, si el
// flujo de origen falla, Failure(mensaje) y termina (sin relanzar). El mensaje es e.message ?: "error".
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T) : UiState<T>
    data class Failure(val message: String) : UiState<Nothing>
}

fun <T> Flow<T>.asUiState(): Flow<UiState<T>> = TODO("Paso 4")

// Paso 5: si el flujo falla, vuelve a recogerlo desde el principio hasta times reintentos más.
// times = 2 significa: 1 intento inicial + 2 reintentos. Si sigue fallando, el error sale tal cual.
fun <T> Flow<T>.retryTimes(times: Long): Flow<T> = TODO("Paso 5")

// Paso 6: anota en log "start" al empezar, "item:<valor>" por cada valor y, al terminar, "end:ok" si fue bien o
// "end:<mensaje del error>" si falló. NO captura el error: lo deja pasar.
fun <T> Flow<T>.logged(log: MutableList<String>): Flow<T> = TODO("Paso 6")

// Paso 7: un flujo que emite el nombre del hilo en el que se ejecuta su cuerpo, pero lo ejecuta en
// Dispatchers.Default aunque se recoja desde otro hilo (flowOn).
fun workerThreadName(): Flow<String> = TODO("Paso 7")

fun main() = runBlocking {
    println(ticker(3, 100).toList())
    println(workerThreadName().toList())
}

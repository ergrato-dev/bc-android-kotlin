import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

// Paso 1: un contador observable. count es un StateFlow de solo lectura que empieza en 0; increment suma 1
// de forma ATÓMICA (MutableStateFlow.update), también desde muchas coroutines a la vez. set pone un valor.
class Counter {
    val count: StateFlow<Int> get() = TODO("Paso 1")

    fun increment(): Unit = TODO("Paso 1")

    fun set(value: Int): Unit = TODO("Paso 1")
}

// Paso 2: un bus de eventos. Los suscriptores reciben lo que se publique MIENTRAS están suscritos; con replay = n,
// un suscriptor tardío recibe además los n últimos. publish suspende si hay que esperar (emit).
class EventBus(replay: Int = 0) {
    val events: SharedFlow<String> get() = TODO("Paso 2")

    suspend fun publish(event: String): Unit = TODO("Paso 2")
}

// Paso 3: convierte un flujo frío en un StateFlow con valor inicial. El flujo de origen solo se recoge mientras haya
// suscriptores, y se sigue recogiendo 5 000 ms después de que se vaya el último (stateIn con WhileSubscribed).
fun <T> Flow<T>.shareAsState(scope: CoroutineScope, initial: T): StateFlow<T> = TODO("Paso 3")

// Paso 4: la lista filtrada por la consulta (contiene el texto, sin distinguir mayúsculas). Se recalcula cuando
// cambia la lista O la consulta (combine). Una consulta vacía deja pasar todo.
fun searchResults(items: Flow<List<String>>, query: Flow<String>): Flow<List<String>> = TODO("Paso 4")

// Paso 5: espera a que el usuario deje de teclear: emite el último valor tras ms sin cambios y no repite valores
// consecutivos iguales (debounce + distinctUntilChanged). debounce está en estado "preview": con -Werror hay que
// aceptarlo con @OptIn(FlowPreview::class) en la función (lección 5).
fun Flow<String>.settled(ms: Long): Flow<String> = TODO("Paso 5")

// Paso 6: por cada consulta lanza search; si llega una consulta NUEVA mientras la anterior sigue buscando, la
// anterior se CANCELA y solo se emite el resultado de la última (flatMapLatest, que pide
// @OptIn(ExperimentalCoroutinesApi::class) en la función).
fun searchLatest(queries: Flow<String>, search: suspend (String) -> List<String>): Flow<List<String>> =
    TODO("Paso 6")

fun main() = runBlocking {
    val counter = Counter()
    println(counter.count.value)
}

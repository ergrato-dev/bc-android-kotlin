import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

// Paso 1: un contador que se puede incrementar desde muchas coroutines a la vez sin perder cuentas.
// Protege el estado con un Mutex (withLock). Quita el Mutex y ejecuta el test: ¿cuánto suma?
class SafeCounter {
    suspend fun increment(): Unit = TODO("Paso 1")

    suspend fun value(): Int = TODO("Paso 1")
}

// Paso 2: un productor que envía 1*1, 2*2, ..., n*n por un Channel y un consumidor que los suma.
// El productor es un launch que hace send de cada cuadrado y close() al terminar; el consumidor recorre el canal con for.
fun CoroutineScope.produceSquares(n: Int): ReceiveChannel<Int> = TODO("Paso 2")

suspend fun sumOfSquares(n: Int): Int = TODO("Paso 2")

// Paso 3: runAll ejecuta todas las tareas a la vez y devuelve un Result por tarea, en orden. El fallo de una
// NO cancela a las demás (supervisorScope).
suspend fun runAll(tasks: List<suspend () -> Int>): List<Result<Int>> = TODO("Paso 3")

// Paso 4: abre un recurso, ejecuta block y lo cierra SIEMPRE, incluso si la coroutine se cancela.
// log recibe "open" al empezar y "close" al terminar. El cierre tarda 50 ms (delay): para que un delay funcione
// dentro de una coroutine ya cancelada hace falta withContext(NonCancellable).
suspend fun withResource(log: MutableList<String>, block: suspend () -> Unit): Unit = TODO("Paso 4")

// Paso 5: transforma los items a la vez, pero NUNCA más de limit transformaciones simultáneas (Semaphore).
// El resultado conserva el orden de items.
suspend fun <T, R> mapLimited(items: List<T>, limit: Int, transform: suspend (T) -> R): List<R> = TODO("Paso 5")

// Paso 6: procesa los items de uno en uno. Es código sin suspensiones (CPU), así que la cancelación solo se
// nota si la compruebas: ensureActive() al empezar cada vuelta.
suspend fun processAll(items: List<Int>, process: (Int) -> Unit): Unit = TODO("Paso 6")

// Paso 7: un scope cuyas coroutines hijas, si fallan, anotan el mensaje del error en errors en lugar de tumbar
fun errorScope(errors: MutableList<String>): CoroutineScope = TODO("Paso 7")

fun main() = runBlocking {
    println(sumOfSquares(4))
    val errors = mutableListOf<String>()
    println(errors)
}

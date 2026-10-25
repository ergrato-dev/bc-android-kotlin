# Estado compartido, límites y canales

> Cuando varias coroutines tocan el mismo dato, `count++` ya no es seguro. Esta lección da las herramientas para compartir estado, limitar la concurrencia y pasar datos entre coroutines.

## 🎯 Objetivos

- Explicar por qué `count++` pierde incrementos y arreglarlo con `Mutex`
- Limitar cuántas coroutines trabajan a la vez con `Semaphore`
- Pasar datos entre coroutines con un `Channel`
- Elegir entre estas herramientas

## 1. La carrera de datos

```kotlin
var count = 0
withContext(Dispatchers.Default) {
    repeat(100) { launch { repeat(1000) { count++ } } }
}
println(count)          // esperabas 100000; sale 96312, 99871...
```

`count++` son tres pasos (leer, sumar, escribir). Dos hilos pueden leer el mismo valor y uno de los incrementos **se pierde**. El resultado cambia en cada ejecución: es de esos fallos que "en mi máquina funciona".

## 2. `Mutex`: un candado que suspende

```kotlin
class SafeCounter {
    private val mutex = Mutex()
    private var count = 0

    suspend fun increment() = mutex.withLock { count++ }
    suspend fun value(): Int = mutex.withLock { count }
}
```

Solo una coroutine está dentro de `withLock` a la vez; las demás **se suspenden** (no bloquean el hilo). A diferencia de `synchronized`, `Mutex` **no es reentrante**: si dentro de `withLock` vuelves a pedir el mismo candado, te quedas esperando para siempre (*deadlock*).

| Alternativa | Cuándo |
| --- | --- |
| `AtomicInteger` | Un solo número y operaciones simples (`incrementAndGet`) |
| `Mutex` | Varias variables que deben cambiar juntas |
| Un único dueño (canal o hilo propio) | El estado es complejo: nadie más lo toca |

Lo mejor sigue siendo **no compartir**: si cada coroutine devuelve su resultado y quien llama los junta, no hay carrera.

## 3. `Semaphore`: limitar la concurrencia

Un `Semaphore(2)` deja pasar a dos coroutines a la vez; la tercera espera un permiso.

```kotlin
suspend fun <T, R> mapLimited(items: List<T>, limit: Int, transform: suspend (T) -> R): List<R> = coroutineScope {
    val permits = Semaphore(limit)
    items.map { item -> async { permits.withPermit { transform(item) } } }.awaitAll()
}
```

Es la herramienta para "pide 50 URLs, pero de 4 en 4". Compárala con `limitedParallelism`: aquella limita **hilos**; el `Semaphore` limita **coroutines** (y puedes tener miles esperando sin gastar hilos).

## 4. `Channel`: pasar datos entre coroutines

Un canal es una cola con suspensión: `send` suspende si no hay quien reciba; `receive` suspende si no hay nada que recibir.

```kotlin
fun CoroutineScope.produceSquares(n: Int): ReceiveChannel<Int> {
    val channel = Channel<Int>()
    launch {
        for (i in 1..n) channel.send(i * i)
        channel.close()                     // sin close, el consumidor espera para siempre
    }
    return channel
}

suspend fun sumOfSquares(n: Int): Int = coroutineScope {
    var sum = 0
    for (square in produceSquares(n)) sum += square      // termina cuando el canal se cierra
    sum
}
```

- `Channel()` por defecto no tiene búfer (*rendezvous*): productor y consumidor se esperan. `Channel(capacity = 10)` deja adelantarse al productor.
- Cada elemento lo recibe **un solo** consumidor. Si quieres que todos vean cada valor, eso es `Flow` (semana 7).
- Hay un constructor `produce { }` que hace estas tres líneas por ti, pero sigue marcado como experimental en la versión del bootcamp.

## 5. Elegir

| Necesitas | Usa |
| --- | --- |
| Que dos coroutines no editen a la vez el mismo dato | `Mutex` (o un atómico si es un número) |
| "No más de N a la vez" | `Semaphore` |
| Un flujo de valores entre productor y consumidor | `Channel` |
| Un flujo de valores observable por varios | `Flow` (semana 7) |
| Nada de lo anterior | No compartas: devuelve resultados |

## 6. Errores frecuentes

| Error | Causa | Arreglo |
| --- | --- | --- |
| La suma da menos de lo esperado, y cambia | Carrera de datos | `Mutex` o `AtomicInteger` |
| El programa se queda colgado en `withLock` | Candado pedido dos veces (no reentrante) | Rediseña: no anides |
| El consumidor del canal nunca termina | Falta `close()` | `channel.close()` al acabar de enviar |
| `Semaphore` y siguen llegando más de N | El `acquire` no está alrededor del trabajo | `withPermit { trabajo }` |
| Todo va lento con `Mutex` | Sección crítica muy grande | Bloquea solo lo imprescindible |

## ✅ Qué debes saber al terminar

- `count++` entre hilos pierde cuentas; `Mutex` las protege.
- `Mutex` suspende y no es reentrante.
- `Semaphore` limita coroutines simultáneas.
- Un `Channel` necesita `close()` y entrega cada valor a un solo consumidor.

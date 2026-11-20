package dev.bitacora.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Tres cosas de la Bitácora que dependen del reloj. En la app esperan segundos; en los tests, nada: el tiempo es
// virtual (teoría 04).

// Repite block mientras lance una IOException, hasta times intentos en total. Antes del segundo espera first ms, antes
// del tercero el doble, y así. Si el último intento también falla, su excepción sale. Cualquier otra excepción (y la
// cancelación) sale a la primera, sin repetir.
suspend fun <T> retrying(times: Int, first: Long, block: suspend () -> T): T = TODO("paso 2")

// Lo que se escribe en la caja de búsqueda, convertido en búsquedas: solo cuando la persona lleva wait ms sin
// escribir, sin espacios alrededor, nunca vacía y nunca dos veces seguidas la misma.
@OptIn(FlowPreview::class)
fun Flow<String>.searches(wait: Long): Flow<String> = TODO("paso 3")

// Pide fetch cada every ms desde start() hasta stop(), en scope, y deja el último resultado en latest. La primera
// petición es al momento. Llamar a start() dos veces no lanza un segundo bucle.
class Poller(private val scope: CoroutineScope, private val every: Long, private val fetch: suspend () -> Int) {
    private val _latest = MutableStateFlow<Int?>(null)
    val latest: StateFlow<Int?> = _latest.asStateFlow()

    fun start(): Unit = TODO("paso 4")

    fun stop(): Unit = TODO("paso 4")
}

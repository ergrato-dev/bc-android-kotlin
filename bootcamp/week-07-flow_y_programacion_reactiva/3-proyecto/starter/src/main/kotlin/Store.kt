import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// La Bitácora reactiva: una sola fuente de verdad que se OBSERVA, en lugar de una lista que se consulta.
class LogbookStore(val domain: String) {
    // Paso 1: la lista actual, en orden de inserción. StateFlow de solo lectura sobre un MutableStateFlow privado.
    val observations: StateFlow<List<Observation>> get() = TODO("Paso 1")

    // Paso 1: añade si no existe (misma Observation ya guardada -> false). Tiene que ser ATÓMICO con update:
    // muchas coroutines pueden añadir a la vez y no se puede perder ninguna. Cuidado: el bloque de update puede
    // ejecutarse más de una vez, así que no cambies nada fuera de él salvo asignar el resultado.
    fun add(o: Observation): Boolean = TODO("Paso 1")

    // Paso 1: quita todas las de ese code y devuelve cuántas quitó (también atómico).
    fun remove(code: Code): Int = TODO("Paso 1")

    // Paso 2: cuántas observaciones hay por nivel, SIEMPRE con los tres niveles (aunque valgan 0). Un flujo que se
    // actualiza solo cuando cambia el contenido de la lista, y no emite dos veces el mismo mapa seguido.
    val countByLevel: Flow<Map<Level, Int>> get() = TODO("Paso 2")
}

// Paso 3: lo que ve la pantalla: las observaciones cuya descripción contiene la consulta (sin distinguir
// mayúsculas; vacía = todas) y cuyo nivel es level (null = cualquiera). Se recalcula cuando cambia el almacén,
// la consulta o el nivel (combine). Conserva el orden de inserción.
fun LogbookStore.watch(query: Flow<String>, level: Flow<Level?>): Flow<List<Observation>> = TODO("Paso 3")

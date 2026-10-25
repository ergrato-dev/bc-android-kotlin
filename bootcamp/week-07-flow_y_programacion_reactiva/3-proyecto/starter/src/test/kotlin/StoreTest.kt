import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreTest {
    private fun obs(code: String, measure: Double = 1.0) = Observation(Code(code), "descripción $code", 1, measure)

    @Test
    fun addRejectsADuplicate() {
        val store = LogbookStore("demo")
        assertTrue(store.add(obs("A-1")))
        assertFalse(store.add(obs("A-1")))
        assertEquals(1, store.observations.value.size)
    }

    // Añade al menos 8 tests más, con valores calculados a mano (runTest y currentTime; para observar un flujo
    // usa UnconfinedTestDispatcher y backgroundScope.launch { flujo.toList(lista) }). Cubre: remove, countByLevel
    // (ceros y que no repita el mismo mapa), watch con consulta y con nivel, syncInto con el orden de llegada y los
    // tiempos (currentTime), un fallo y un tiempo agotado, duplicadas e inválidas, take(1) cancelando el resto,
    // los require, muchas coroutines añadiendo a la vez y summarize.
}

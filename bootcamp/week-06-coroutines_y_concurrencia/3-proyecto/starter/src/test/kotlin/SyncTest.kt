@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncTest {
    @Test
    fun sourcesRunInParallel() = runTest {
        val sources = mapOf(
            "a" to slowSource(100, "A-1;uno;1;1.0"),
            "b" to slowSource(100, "B-1;dos;1;2.0"),
            "c" to slowSource(100, "C-1;tres;1;3.0"),
        )
        val result = sync("demo", sources, maxParallel = 3)
        assertEquals(3, result.logbook.size)
        assertEquals(100, currentTime)
    }

    // Añade al menos 8 tests más, con valores calculados a mano (usa runTest y currentTime, no Thread.sleep).
    // Cubre: maxParallel = 1 (tiempo = suma), maxParallel intermedio, el tiempo límite (motivo exacto y currentTime),
    // que el tiempo límite cuente desde que la fuente empieza, una excepción con mensaje y otra sin mensaje,
    // que un fallo no cancele a las demás, duplicadas e inválidas en orden, los require y que cancelar
    // la sincronización entera NO la convierta en un Failed.
}

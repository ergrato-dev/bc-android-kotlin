import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LogbookTest {
    @Test
    fun observationRejectsABlankCode() {
        assertFailsWith<IllegalArgumentException> { Observation("", "x", 1, 1.0) }
    }

    // Añade al menos 8 tests más, con valores calculados a mano. Cubre: cada regla del init,
    // parse válida e inválida, el orden natural, level como propiedad, add duplicado, remove con conteo,
    // que observations es una copia, el texto exacto de report y fromLines.
}

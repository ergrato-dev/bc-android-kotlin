import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LogbookTest {
    @Test
    fun aBlankCodeIsRejected() {
        assertFailsWith<IllegalArgumentException> { Code("  ") }
    }

    // Añade al menos 9 tests más, con valores calculados a mano. Cubre: Level.of en los límites y con valores
    // inválidos, Code (orden), parseLine con un caso válido y cada motivo de rechazo, countByLevel (con ceros),
    // el texto exacto de report, fromLines con varios descartes y topBy con dos claves distintas.
}

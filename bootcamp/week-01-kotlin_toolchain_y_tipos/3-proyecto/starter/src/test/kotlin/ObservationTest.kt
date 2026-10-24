import kotlin.test.Test
import kotlin.test.assertEquals

class ObservationTest {
    @Test
    fun headerNamesTheDomain() = assertEquals("== Bitácora de campo: agro ==", header("agro"))

    // Añade al menos 4 tests más, con valores calculados a mano (no copiados de la salida).
}

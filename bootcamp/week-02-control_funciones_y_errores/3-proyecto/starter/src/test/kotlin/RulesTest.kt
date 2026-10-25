import kotlin.test.Test
import kotlin.test.assertEquals

class RulesTest {
    @Test
    fun levelIsLowBelowTheMediumThreshold() = assertEquals("LOW", level(9.99))

    // Añade al menos 6 tests más, con valores calculados a mano. Cubre:
    // los límites de level (10.0 y 50.0), require en level y validated (assertFailsWith),
    // parseObservation válida e inválida (null), parseValid, countInvalid y summary.
}

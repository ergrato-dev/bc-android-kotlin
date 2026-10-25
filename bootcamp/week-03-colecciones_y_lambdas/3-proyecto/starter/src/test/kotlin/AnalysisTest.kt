import kotlin.test.Test
import kotlin.test.assertEquals

class AnalysisTest {
    private val sample = listOf(
        Observation("A", "uno", 1, 5.0),
        Observation("B", "dos", 2, 20.0),
    )

    @Test
    fun countByLevelAlwaysHasThreeKeys() =
        assertEquals(mapOf("LOW" to 1, "MEDIUM" to 1, "HIGH" to 0), countByLevel(sample))

    // Añade al menos 7 tests más, con valores calculados a mano. Cubre cada función de Analysis.kt,
    // los empates de top, la lista vacía y el texto exacto de distributionReport.
}

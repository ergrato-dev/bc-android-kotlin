import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    private val sample = logbook("agro") {
        observation("A-1") { description = "Riego norte"; quantity = 3; measure = 12.5 }
        observation("A-2") { description = "Plaga sur"; measure = 4.0 }
        observation("A-3") { description = "Riego sur"; quantity = 2; measure = 75.0 }
    }

    @Test
    fun observationBuilderFillsDefaults() {
        val o = observation("A-1") { description = "Riego" }
        assertEquals(Observation("A-1", "Riego", 1, 0.0), o)
        assertEquals(Observation("A-2", "Plaga", 4, 9.5), observation("A-2") { description = "Plaga"; quantity = 4; measure = 9.5 })
    }

    @Test
    fun observationNeedsADescription() {
        val e = assertFailsWith<IllegalStateException> { observation("A-1") { quantity = 2 } }
        assertEquals("description obligatoria en A-1", e.message)
    }

    @Test
    fun logbookKeepsOrderAndAllowsEmpty() {
        assertEquals(listOf("A-1", "A-2", "A-3"), sample.observations.map { it.code })
        assertEquals("agro", sample.domain)
        assertEquals(emptyList(), logbook("vacío") { }.observations)
    }

    @Test
    fun logbookRejectsARepeatedCode() {
        val e = assertFailsWith<IllegalArgumentException> {
            logbook("agro") {
                observation("A-1") { description = "uno" }
                observation("A-1") { description = "otra vez" }
            }
        }
        assertEquals("code repetido: A-1", e.message)
    }

    @Test
    fun filtersCombineWithAnd() {
        val high = filter { level(Level.HIGH) }
        assertEquals(listOf("A-3"), sample.observations.filter(high).map { it.code })
        val riego = filter { describedAs("RIEGO"); measureAtLeast(12.5) }
        assertEquals(listOf("A-1", "A-3"), sample.observations.filter(riego).map { it.code })
        val nothing = filter { }
        assertEquals(3, sample.observations.count(nothing))
        assertFalse(filter { level(Level.LOW); measureAtLeast(5.0) }(sample.observations[1]))
    }

    @Test
    fun reportRendersSections() {
        val text = report {
            title("Visitas")
            section("Altas") {
                +"uno"
                line("dos")
            }
            section("Bajas") { +"tres" }
        }
        assertEquals("# Visitas\n\n## Altas\n- uno\n- dos\n\n## Bajas\n- tres", text)
        assertEquals("# Solo título", report { title("Solo título") })
    }

    @Test
    fun logbookOperators() {
        assertEquals("Plaga sur", sample["A-2"]?.description)
        assertNull(sample["Z-9"])
        assertTrue("A-3" in sample)
        assertFalse("Z-9" in sample)
        val other = logbook("agro") {
            observation("A-3") { description = "distinta" }
            observation("B-1") { description = "nueva" }
        }
        val merged = sample + other
        assertEquals(listOf("A-1", "A-2", "A-3", "B-1"), merged.observations.map { it.code })
        assertEquals("Riego sur", merged["A-3"]?.description)
        val e = assertFailsWith<IllegalArgumentException> { sample + logbook("otro") { } }
        assertEquals("domains distintos: agro e otro", e.message)
    }

    @Test
    fun summaryIsBuiltWithTheJsonDsl() {
        assertEquals(
            """{"domain":"agro","total":3,"levels":{"LOW":1,"MEDIUM":1,"HIGH":1},"codes":["A-1","A-2","A-3"]}""",
            summaryJson(sample),
        )
        assertEquals(
            """{"domain":"vacío","total":0,"levels":{"LOW":0,"MEDIUM":0,"HIGH":0},"codes":[]}""",
            summaryJson(logbook("vacío") { }),
        )
    }
}

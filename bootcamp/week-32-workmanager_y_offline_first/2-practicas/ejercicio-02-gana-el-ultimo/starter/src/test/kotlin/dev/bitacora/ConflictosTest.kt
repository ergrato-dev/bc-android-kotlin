package dev.bitacora

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConflictosTest {
    private val ana = Version("n1", "roya en el lote 2", at = 1_000, device = "ana")
    private val luis = Version("n1", "roya en el lote 3", at = 2_000, device = "luis")

    @Test
    fun theNewerWins() {
        assertTrue(luis.beats(ana))
        assertFalse(ana.beats(luis))
    }

    // Misma hora: decide el nombre del móvil, y solo una de las dos gana.
    @Test
    fun atTheSameTimeTheDeviceDecides() {
        val tie = luis.copy(at = 1_000)
        assertTrue(tie.beats(ana))
        assertFalse(ana.beats(tie))
    }

    @Test
    fun aVersionDoesNotBeatItself() {
        assertFalse(ana.beats(ana))
    }

    @Test
    fun mergeKeepsTheWinnerOfEachNote() {
        val other = Version("n2", "riego", at = 500, device = "ana")
        assertEquals(listOf(luis, other), merge(listOf(ana, other), listOf(luis)))
    }

    // Borrar después de la edición de Luis: la lápida gana, se queda en la lista y no se ve.
    @Test
    fun aNewerDeletionWins() {
        val deleted = Version("n1", null, at = 3_000, device = "ana")
        assertEquals(listOf(deleted), merge(listOf(deleted), listOf(luis)))
        assertEquals(emptyMap(), merge(listOf(deleted), listOf(luis)).visible())
    }

    // Y borrar antes: la edición de Luis, más nueva, la devuelve.
    @Test
    fun anOlderDeletionLoses() {
        val deleted = Version("n1", null, at = 1_500, device = "ana")
        assertEquals(mapOf("n1" to "roya en el lote 3"), merge(listOf(deleted), listOf(luis)).visible())
    }

    // El móvil y el servidor llegan a lo mismo, se junte en el orden que se junte: si no, cada uno se quedaría con una.
    @Test
    fun theOrderDoesNotMatter() {
        val tie = luis.copy(at = 1_000)
        assertEquals(merge(listOf(ana), listOf(tie)), merge(listOf(tie), listOf(ana)))
        assertEquals(merge(listOf(ana), listOf(luis)), merge(listOf(luis), listOf(ana)))
    }

    // Sincronizar dos veces no cambia nada: un reintento de WorkManager no estropea nada.
    @Test
    fun mergingTwiceChangesNothing() {
        val once = merge(listOf(ana), listOf(luis))
        assertEquals(once, merge(once, listOf(luis)))
    }

    @Test
    fun theClockNeverRepeats() {
        val clock = Clock { 1_000 }
        assertEquals(listOf(1_000L, 1_001L, 1_002L), List(3) { clock.now() })
    }

    @Test
    fun theClockNeverGoesBack() {
        val times = ArrayDeque(listOf(2_000L, 1_500L))
        val clock = Clock { times.removeFirst() }
        assertEquals(listOf(2_000L, 2_001L), List(2) { clock.now() })
    }

    // El punto débil: el móvil de Luis va una hora adelantado. Ana escribe de verdad después (a las 10:30), pero su
    // hora (10:30) es menor que la de Luis (10:10 de verdad, 11:10 en su móvil): gana Luis.
    @Test
    fun aClockAheadAlwaysWins() {
        val luisAhead = Version("n1", "de Luis, a las 10:10", at = (11 * 60 + 10) * 60_000L, device = "luis")
        val anaLater = Version("n1", "de Ana, a las 10:30", at = (10 * 60 + 30) * 60_000L, device = "ana")
        assertEquals(mapOf("n1" to "de Luis, a las 10:10"), merge(listOf(anaLater), listOf(luisAhead)).visible())
    }
}

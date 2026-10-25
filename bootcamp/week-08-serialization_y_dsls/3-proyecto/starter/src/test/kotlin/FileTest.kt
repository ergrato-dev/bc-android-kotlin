import kotlin.test.Test
import kotlin.test.assertEquals

class FileTest {
    private val one = Observation(Code("A-1"), "Riego", 3, 12.5)

    @Test
    fun theGoldenFile() {
        val file = LogbookFile(domain = "agro", observations = listOf(one), counts = mapOf(Level.MEDIUM to 1))
        assertEquals(
            """{"version":1,"domain":"agro","observations":[{"code":"A-1","description":"Riego","quantity":3,"measure":12.5}],"counts":{"medium":1}}""",
            encodeFile(file),
        )
    }

    // Añade al menos 8 tests más, con valores calculados a mano. Cubre: el JSON de un Code (texto suelto) y de un
    // Level (minúsculas), el viaje de ida y vuelta (DSL -> encodeFile -> importFile), version ausente y claves
    // desconocidas, versión futura, JSON roto y raíz que no es objeto, observaciones rotas con su posición y su motivo
    // (campo ausente, init, tipo equivocado, elemento que no es objeto), el orden de lo válido y lo inválido, y el
    // DSL (code repetido, description ausente, domain en blanco, counts).
}

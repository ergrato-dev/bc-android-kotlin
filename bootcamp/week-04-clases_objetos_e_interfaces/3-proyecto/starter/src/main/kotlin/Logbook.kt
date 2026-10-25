// Paso 4: la Bitácora como objeto con estado propio. Por fuera solo se ve una List de solo lectura (copia).
// add: false si ya hay una observación IGUAL (data class), true si la añade.
// remove: borra todas las de ese code y devuelve cuántas borró.
// countByLevel: SIEMPRE las claves LOW, MEDIUM y HIGH, aunque valgan 0.
// report: header(domain), una línea por observación en el orden de inserción con el formato
//   "<code> | <description> | <quantity> | <measure con 2 decimales> | <level>"
// y una última línea "Total: N".
// fromLines (companion): construye una Bitácora con las líneas válidas (Observation.parse) y descarta el resto.
class Logbook(val domain: String) : Reportable {
    private val _observations = mutableListOf<Observation>()

    val observations: List<Observation> get() = TODO("Paso 4")

    val size: Int get() = TODO("Paso 4")

    fun add(o: Observation): Boolean = TODO("Paso 4")

    fun remove(code: String): Int = TODO("Paso 4")

    fun countByLevel(): Map<String, Int> = TODO("Paso 4")

    override fun report(): String = TODO("Paso 4")

    companion object {
        fun fromLines(domain: String, lines: List<String>): Logbook = TODO("Paso 4")
    }
}

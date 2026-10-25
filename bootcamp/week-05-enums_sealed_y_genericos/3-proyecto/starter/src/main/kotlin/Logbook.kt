// Lo que devuelve la lectura de líneas: la bitácora con lo válido y la lista de lo descartado, con su motivo.
data class ImportResult(val logbook: Logbook, val invalid: List<ParseResult.Invalid>)

class Logbook(val domain: String) : Reportable {
    private val _observations = mutableListOf<Observation>()

    val observations: List<Observation> get() = _observations.toList()

    val size: Int get() = _observations.size

    fun add(o: Observation): Boolean {
        if (o in _observations) return false
        _observations.add(o)
        return true
    }

    fun remove(code: Code): Int {
        val before = size
        _observations.removeAll { it.code == code }
        return before - size
    }

    // Paso 5: SIEMPRE los tres niveles como claves, aunque valgan 0 (usa Level.entries).
    fun countByLevel(): Map<Level, Int> = TODO("Paso 5")

    // Paso 5: header(domain), una línea por observación en orden de inserción con el formato
    // "<code> | <description> | <quantity> | <measure con 2 decimales> | <level.name>" y "Total: N".
    override fun report(): String = TODO("Paso 5")

    companion object {
        // Paso 6: usa parseLine; lo válido se añade, lo inválido se guarda en ImportResult.invalid (en orden).
        fun fromLines(domain: String, lines: List<String>): ImportResult = TODO("Paso 6")
    }
}

// Paso 7: función genérica con restricción. Las n observaciones con MAYOR key(it), de mayor a menor;
// en empate, por code ascendente. n negativo: require. Prueba con key = { it.measure } y key = { it.quantity }.
fun <T : Comparable<T>> topBy(list: List<Observation>, n: Int, key: (Observation) -> T): List<Observation> =
    TODO("Paso 7")

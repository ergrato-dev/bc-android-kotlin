class Logbook(val domain: String) : Reportable {
    private val _observations = mutableListOf<Observation>()

    val observations: List<Observation> get() = _observations.toList()

    val size: Int get() = _observations.size

    fun add(o: Observation): Boolean {
        if (o in _observations) return false
        _observations.add(o)
        return true
    }

    fun countByLevel(): Map<Level, Int> {
        val counts = observations.groupingBy { it.level }.eachCount()
        return Level.entries.associateWith { counts[it] ?: 0 }
    }

    override fun report(): String = buildString {
        appendLine(header(domain))
        observations.forEach {
            appendLine("${it.code} | ${it.description} | ${it.quantity} | ${it.measure.format2()} | ${it.level.name}")
        }
        append("Total: $size")
    }
}

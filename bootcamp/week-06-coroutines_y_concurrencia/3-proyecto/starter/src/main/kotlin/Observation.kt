data class Observation(
    val code: Code,
    val description: String,
    val quantity: Int,
    val measure: Double,
) : Comparable<Observation> {
    init {
        require(description.isNotBlank()) { "description en blanco" }
        require(quantity >= 0) { "quantity negativa: $quantity" }
        require(!measure.isNaN() && measure >= 0) { "measure inválida: $measure" }
    }

    override fun compareTo(other: Observation): Int = compareValuesBy(this, other, { it.measure }, { it.code })
}

val Observation.level: Level get() = Level.of(measure)

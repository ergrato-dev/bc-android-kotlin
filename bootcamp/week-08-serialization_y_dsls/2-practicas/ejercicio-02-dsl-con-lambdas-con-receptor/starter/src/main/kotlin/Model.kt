// Base de este ejercicio: el modelo ya está hecho. Lo que construyes en Main.kt son los DSL.
enum class Level { LOW, MEDIUM, HIGH }

data class Observation(val code: String, val description: String, val quantity: Int, val measure: Double) {
    init {
        require(code.isNotBlank()) { "code en blanco" }
        require(description.isNotBlank()) { "description en blanco" }
        require(quantity >= 0) { "quantity negativa: $quantity" }
        require(measure >= 0) { "measure negativa: $measure" }
    }
}

// LOW por debajo de 10, MEDIUM de 10 a menos de 50, HIGH desde 50.
val Observation.level: Level
    get() = when {
        measure < 10.0 -> Level.LOW
        measure < 50.0 -> Level.MEDIUM
        else -> Level.HIGH
    }

data class Logbook(val domain: String, val observations: List<Observation>)

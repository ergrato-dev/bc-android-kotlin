// Paso 1: la observación se valida SOLA al crearla (init). Reglas: code y description no en blanco,
// quantity >= 0, measure >= 0 y no NaN. Cada require con un mensaje que diga qué valor llegó.
// Orden natural: por measure y, en empate, por code (compareValuesBy).
data class Observation(
    val code: String,
    val description: String,
    val quantity: Int,
    val measure: Double,
) : Comparable<Observation> {
    init {
        TODO("Paso 1: validar")
    }

    override fun compareTo(other: Observation): Int = TODO("Paso 1")

    companion object {
        // Paso 2: "CODE;descripción;cantidad;medida" -> Observation, o null si la línea no es válida
        // (no tiene 4 partes, cantidad o medida no numéricas, o el init la rechaza).
        fun parse(line: String): Observation? = TODO("Paso 2")
    }
}

// Paso 3: propiedad de extensión: el nivel de esta observación (usa la función level de Base.kt).
val Observation.level: String get() = TODO("Paso 3")

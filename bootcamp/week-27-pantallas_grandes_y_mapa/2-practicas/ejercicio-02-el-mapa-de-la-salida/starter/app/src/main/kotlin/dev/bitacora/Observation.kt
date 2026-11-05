package dev.bitacora

// Ya está hecho: las observaciones de la salida. A-2 se apuntó sin ubicación.
data class Observation(val code: String, val level: Level, val description: String, val latitude: Double? = null, val longitude: Double? = null)

val observations = listOf(
    Observation("A-1", Level.HIGH, "riego roto en el sector norte", -34.6037, -58.3816),
    Observation("A-2", Level.MEDIUM, "hojas amarillas en la parcela 3"),
    Observation("A-3", Level.LOW, "pulgón en los tomates", -34.6118, -58.4173),
)

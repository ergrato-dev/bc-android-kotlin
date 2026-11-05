package dev.bitacora

// Ya está hecho: las observaciones de la sesión.
data class Observation(val code: String, val level: Level, val description: String)

val observations = listOf(
    Observation("A-1", Level.HIGH, "riego roto en el sector norte"),
    Observation("A-2", Level.MEDIUM, "hojas amarillas en la parcela 3"),
    Observation("A-3", Level.LOW, "pulgón en los tomates"),
)

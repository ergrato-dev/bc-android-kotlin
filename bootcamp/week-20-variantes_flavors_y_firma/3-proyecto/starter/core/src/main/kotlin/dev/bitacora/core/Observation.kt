package dev.bitacora.core

enum class Level { LOW, MEDIUM, HIGH }

data class Position(val latitude: Double, val longitude: Double)

// La foto es el nombre del archivo, no el archivo: dónde se guarda lo decide la app.
data class Observation(
    val code: String,
    val level: Level,
    val description: String,
    val position: Position? = null,
    val photo: String? = null,
)

fun List<Observation>.countByLevel(): Map<Level, Int> = Level.entries.associateWith { l -> count { it.level == l } }

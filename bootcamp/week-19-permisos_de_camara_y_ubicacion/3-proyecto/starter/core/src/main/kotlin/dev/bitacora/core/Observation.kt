package dev.bitacora.core

enum class Level { LOW, MEDIUM, HIGH }

// Paso 1: Position (latitud y longitud) y dos campos opcionales: position y photo (el nombre del archivo).
data class Observation(val code: String, val level: Level, val description: String)

fun List<Observation>.countByLevel(): Map<Level, Int> = Level.entries.associateWith { l -> count { it.level == l } }

package dev.bitacora.core

enum class Level { LOW, MEDIUM, HIGH }

data class Observation(val code: String, val level: Level, val description: String)

fun List<Observation>.countByLevel(): Map<Level, Int> = Level.entries.associateWith { l -> count { it.level == l } }

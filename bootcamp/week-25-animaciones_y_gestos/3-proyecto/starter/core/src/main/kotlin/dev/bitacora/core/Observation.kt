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

// El código de la siguiente.
// Paso 1: uno más que el mayor, no que el tamaño: con A-1 y A-3, la siguiente es A-4. Si no, al borrar se repite uno.
fun List<Observation>.nextCode(): String = "A-${size + 1}"

fun List<Observation>.countByLevel(): Map<Level, Int> = Level.entries.associateWith { l -> count { it.level == l } }

// Lo más largo que puede ser una descripción: la regla es del dominio, la pantalla solo la enseña.
const val MAX_DESCRIPTION = 80

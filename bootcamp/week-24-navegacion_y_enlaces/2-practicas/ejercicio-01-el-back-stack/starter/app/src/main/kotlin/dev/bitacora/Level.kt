package dev.bitacora

// Ya está hecho: los niveles, su texto y la letra de su insignia.
enum class Level(val label: Int, val short: Int) {
    LOW(R.string.level_low, R.string.level_low_short),
    MEDIUM(R.string.level_medium, R.string.level_medium_short),
    HIGH(R.string.level_high, R.string.level_high_short),
}

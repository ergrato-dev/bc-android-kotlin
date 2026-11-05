package dev.bitacora

// Ya está hecho: los niveles, su texto, la letra de su insignia y su color (res/values/colors.xml), y el siguiente.
enum class Level(val label: Int, val short: Int, val color: Int) {
    LOW(R.string.level_low, R.string.level_low_short, R.color.level_low),
    MEDIUM(R.string.level_medium, R.string.level_medium_short, R.color.level_medium),
    HIGH(R.string.level_high, R.string.level_high_short, R.color.level_high),
    ;

    val next: Level get() = entries[(ordinal + 1) % entries.size]
}

package dev.bitacora

import dev.bitacora.core.Level

// core no sabe nada de Android: el texto de cada nivel lo decide la app.
internal val Level.label: Int
    get() = when (this) {
        Level.LOW -> R.string.level_low
        Level.MEDIUM -> R.string.level_medium
        Level.HIGH -> R.string.level_high
    }

// La letra de la insignia de cada nivel.
internal val Level.short: Int
    get() = when (this) {
        Level.LOW -> R.string.level_low_short
        Level.MEDIUM -> R.string.level_medium_short
        Level.HIGH -> R.string.level_high_short
    }

internal val Level.color: Int
    get() = when (this) {
        Level.LOW -> R.color.level_low
        Level.MEDIUM -> R.color.level_medium
        Level.HIGH -> R.color.level_high
    }

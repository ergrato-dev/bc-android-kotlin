package dev.bitacora

import dev.bitacora.core.Level

// core no sabe nada de Android: el texto de cada nivel lo decide la app.
internal val Level.label: Int
    get() = when (this) {
        Level.LOW -> R.string.level_low
        Level.MEDIUM -> R.string.level_medium
        Level.HIGH -> R.string.level_high
    }

package dev.bitacora

// Ya está hecho: lo que la persona elige para sus salidas.
enum class Units { METRIC, IMPERIAL }

data class TripSettings(val units: Units = Units.METRIC, val reminders: Boolean = false)

package dev.bitacora

// Paso 1: @Parcelize y Parcelable, para que viaje entera en un extra.
data class Note(val code: String, val level: String, val text: String)

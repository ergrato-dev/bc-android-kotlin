package dev.bitacora.data

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import dev.bitacora.core.Position
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Los campos nuevos tienen valor por defecto: el JSON de antes, sin ellos, se sigue leyendo.
@Serializable
private data class ObservationDto(
    val code: String,
    val level: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photo: String? = null,
)

private val json = Json { encodeDefaults = false }

fun encode(observations: List<Observation>): String = json.encodeToString(observations.map {
    ObservationDto(it.code, it.level.name, it.description, it.position?.latitude, it.position?.longitude, it.photo)
})

fun decode(text: String): List<Observation> = json.decodeFromString<List<ObservationDto>>(text).map {
    val position = if (it.latitude != null && it.longitude != null) Position(it.latitude, it.longitude) else null
    Observation(it.code, Level.valueOf(it.level), it.description, position, it.photo)
}

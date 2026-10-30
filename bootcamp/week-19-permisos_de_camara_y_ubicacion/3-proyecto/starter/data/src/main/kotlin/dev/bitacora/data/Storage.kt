package dev.bitacora.data

import dev.bitacora.core.Level
import dev.bitacora.core.Observation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Paso 2: los campos nuevos en el DTO, opcionales. El JSON de antes, sin ellos, se tiene que seguir leyendo.
@Serializable
private data class ObservationDto(val code: String, val level: String, val description: String)

private val json = Json { encodeDefaults = false }

fun encode(observations: List<Observation>): String =
    json.encodeToString(observations.map { ObservationDto(it.code, it.level.name, it.description) })

fun decode(text: String): List<Observation> =
    json.decodeFromString<List<ObservationDto>>(text).map { Observation(it.code, Level.valueOf(it.level), it.description) }

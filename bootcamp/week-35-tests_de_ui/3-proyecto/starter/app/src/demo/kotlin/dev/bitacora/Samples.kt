package dev.bitacora

import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.model.Position

// Solo en demo: la Bitácora arranca con tres observaciones, para enseñarla sin salir al campo.
object Samples {
    val observations = listOf(
        Observation("A-1", Level.HIGH, "riego roto en el sector norte", Position(-34.6037, -58.3816)),
        Observation("A-2", Level.MEDIUM, "hojas amarillas en la parcela 3"),
        Observation("A-3", Level.LOW, "pulgón en los tomates"),
    )
}

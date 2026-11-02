package dev.bitacora

// Ya está hecho: lo que enseña una fila. La ubicación llega ya escrita ("-34.6037, -58.3816"), o null.
data class Observation(val code: String, val level: Level, val description: String, val place: String? = null)

val samples = listOf(
    Observation("A-1", Level.HIGH, "riego roto en el sector norte", "-34.6037, -58.3816"),
    Observation("A-2", Level.MEDIUM, "hojas amarillas en la parcela 3"),
    Observation("A-3", Level.LOW, "pulgón en los tomates"),
)

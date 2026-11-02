package dev.bitacora

// Ya está hecho: lo que enseña una fila. La ubicación llega ya escrita ("-34.6037, -58.3816"), o null.
data class Observation(val code: String, val level: Level, val description: String, val place: String? = null)

// Una sesión larga: 500 observaciones, de la A-1 a la A-500, con los niveles alternados.
val longSession = List(500) { i ->
    val n = i + 1
    Observation("A-$n", Level.entries[i % 3], "observación $n", if (n % 2 == 0) "-34.$n, -58.$n" else null)
}

data class Observation(
    val code: String,
    val description: String,
    val quantity: Int,
    val measure: Double,
)

const val COLUMN_WIDTH = 8

fun header(domain: String): String = "== Bitácora de campo: $domain =="

// Una fila de la tabla: código a la izquierda (8), descripción a la izquierda (24),
// cantidad a la derecha (5), medida a la derecha con 2 decimales (10).
fun row(o: Observation): String = TODO("Proyecto: fila alineada con String.format(Locale.ROOT, ...); recuerda el import de java.util.Locale")

// Suma de quantity * measure de todas las observaciones.
fun total(list: List<Observation>): Double = TODO("Proyecto")

// Media de measure. División real; lista vacía devuelve 0.0.
fun average(list: List<Observation>): Double = TODO("Proyecto")

// Cabecera + una fila por observación + línea de total y media con 2 decimales.
fun report(domain: String, list: List<Observation>): String = TODO("Proyecto")

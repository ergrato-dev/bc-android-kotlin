import java.util.Locale

// Base de la semana 1. Si ya tienes tu Observation.kt, úsalo en su lugar.
data class Observation(
    val code: String,
    val description: String,
    val quantity: Int,
    val measure: Double,
)

fun header(domain: String): String = "== Bitácora de campo: $domain =="

fun row(o: Observation): String =
    String.format(Locale.ROOT, "%-8s %-24s %5d %10.2f", o.code, o.description, o.quantity, o.measure)

fun total(list: List<Observation>): Double = list.sumOf { it.quantity * it.measure }

fun average(list: List<Observation>): Double =
    if (list.isEmpty()) 0.0 else list.sumOf { it.measure } / list.size

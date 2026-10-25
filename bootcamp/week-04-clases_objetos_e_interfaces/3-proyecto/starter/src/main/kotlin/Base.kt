import java.util.Locale

// Base de las semanas 1 a 3. Si ya tienes tus versiones, úsalas en lugar de este archivo.
fun header(domain: String): String = "== Bitácora de campo: $domain =="

fun Double.format2(): String = String.format(Locale.ROOT, "%.2f", this)

fun level(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): String {
    require(!measure.isNaN() && measure >= 0) { "measure inválida: $measure" }
    return when {
        measure >= highFrom -> "HIGH"
        measure >= mediumFrom -> "MEDIUM"
        else -> "LOW"
    }
}

data class Stats(val count: Int, val min: Double, val max: Double, val average: Double)

fun statistics(list: List<Observation>): Stats? =
    if (list.isEmpty()) null
    else Stats(list.size, list.minOf { it.measure }, list.maxOf { it.measure }, list.sumOf { it.measure } / list.size)

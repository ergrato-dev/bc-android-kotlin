// Base de las semanas 1 y 2. Si ya tienes tus versiones, úsalas en lugar de este archivo.
data class Observation(
    val code: String,
    val description: String,
    val quantity: Int,
    val measure: Double,
)

fun header(domain: String): String = "== Bitácora de campo: $domain =="

fun level(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): String {
    require(!measure.isNaN() && measure >= 0) { "measure inválida: $measure" }
    return when {
        measure >= highFrom -> "HIGH"
        measure >= mediumFrom -> "MEDIUM"
        else -> "LOW"
    }
}

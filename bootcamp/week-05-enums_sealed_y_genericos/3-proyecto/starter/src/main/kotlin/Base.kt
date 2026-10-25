import java.util.Locale

// Base de semanas anteriores. Si ya tienes tus versiones, úsalas en lugar de este archivo.
fun header(domain: String): String = "== Bitácora de campo: $domain =="

fun Double.format2(): String = String.format(Locale.ROOT, "%.2f", this)

interface Reportable {
    fun report(): String
}

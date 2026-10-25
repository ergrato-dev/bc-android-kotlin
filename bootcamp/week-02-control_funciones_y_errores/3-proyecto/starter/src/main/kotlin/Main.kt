// Sustituye las líneas por datos de tu dominio. Una de ellas debe ser inválida a propósito.
fun main() {
    val lines = listOf(
        "OBS-001;Ejemplo uno;3;12.5",
        "OBS-002;Ejemplo dos;1;57.25",
        "OBS-003;;4;1.0",
        "OBS-004;Ejemplo cuatro;diez;2.0",
    )
    println(summary("MI-DOMINIO", lines))
}

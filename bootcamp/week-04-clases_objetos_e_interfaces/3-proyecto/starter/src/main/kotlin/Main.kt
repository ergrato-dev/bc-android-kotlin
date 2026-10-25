// Sustituye las líneas por datos de tu dominio. Una debe ser inválida a propósito.
fun main() {
    val logbook = Logbook.fromLines(
        "MI-DOMINIO",
        listOf(
            "OBS-001;Ejemplo uno;3;12.5",
            "OBS-002;Ejemplo dos;1;57.25",
            "OBS-003;;4;1.0",
        ),
    )
    println(logbook.report())
    println(logbook.countByLevel())
    println(statistics(logbook.observations))
}

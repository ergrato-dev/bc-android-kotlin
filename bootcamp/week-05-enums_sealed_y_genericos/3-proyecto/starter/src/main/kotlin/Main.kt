// Sustituye las líneas por datos de tu dominio. Varias deben ser inválidas por motivos distintos.
fun main() {
    val result = Logbook.fromLines(
        "MI-DOMINIO",
        listOf(
            "OBS-001;Ejemplo uno;3;12.5",
            "OBS-002;Ejemplo dos;1;57.25",
            ";Sin código;4;1.0",
            "OBS-004;Cantidad rota;diez;2.0",
        ),
    )
    println(result.logbook.report())
    println(result.logbook.countByLevel())
    result.invalid.forEach { println("Descartada: ${it.line} -> ${it.reason}") }
}

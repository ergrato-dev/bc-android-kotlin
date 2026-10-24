// Sustituye los datos por observaciones de tu dominio.
fun main() {
    val observations = listOf(
        Observation("OBS-001", "Ejemplo uno", 3, 12.5),
        Observation("OBS-002", "Ejemplo dos", 1, 7.25),
        Observation("OBS-003", "Ejemplo tres", 10, 0.8),
    )
    println(report("MI-DOMINIO", observations))
}

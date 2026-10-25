// Sustituye los datos por observaciones de tu dominio.
fun main() {
    val list = listOf(
        Observation("OBS-001", "Ejemplo uno", 3, 12.5),
        Observation("OBS-002", "Ejemplo dos", 1, 57.25),
        Observation("OBS-003", "Ejemplo tres", 4, 1.0),
        Observation("OBS-001", "Ejemplo uno bis", 2, 9.0),
    )
    println(distributionReport("MI-DOMINIO", list))
}

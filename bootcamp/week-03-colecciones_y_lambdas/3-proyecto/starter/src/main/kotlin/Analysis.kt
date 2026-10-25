data class Stats(val count: Int, val min: Double, val max: Double, val average: Double)

// Las observaciones agrupadas por su nivel (level de la medida). Solo aparecen los niveles que tienen alguna.
fun byLevel(list: List<Observation>): Map<String, List<Observation>> = TODO("Proyecto")

// Cuántas observaciones hay en cada nivel. SIEMPRE las tres claves LOW, MEDIUM y HIGH, aunque sea con 0.
fun countByLevel(list: List<Observation>): Map<String, Int> = TODO("Proyecto")

// Las n de mayor measure, de mayor a menor. En empate, por code ascendente. n negativo: require.
fun top(list: List<Observation>, n: Int): List<Observation> = TODO("Proyecto")

// La suma de quantity por cada code (un code puede aparecer varias veces).
fun quantityByCode(list: List<Observation>): Map<String, Int> = TODO("Proyecto")

// Las que contienen text en la descripción, sin distinguir mayúsculas. Texto en blanco: lista vacía.
fun search(list: List<Observation>, text: String): List<Observation> = TODO("Proyecto")

// count, min, max y media de measure; null si la lista está vacía.
fun statistics(list: List<Observation>): Stats? = TODO("Proyecto")

// Cabecera; una línea "LOW: 2" por nivel, en el orden LOW, MEDIUM, HIGH;
// una línea "Top: C1, C2, C3" con los codes de top(list, 3), separados por ", " ("Top: -" si no hay);
// y una línea "Media: X.XX  Máx: Y.YY" con 2 decimales (Locale.ROOT) o "Sin datos" si statistics es null.
fun distributionReport(domain: String, list: List<Observation>): String = TODO("Proyecto")

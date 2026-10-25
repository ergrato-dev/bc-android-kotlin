// "LOW" si measure < mediumFrom, "MEDIUM" si mediumFrom <= measure < highFrom, "HIGH" si measure >= highFrom.
// Una medida negativa o NaN no tiene sentido: require con mensaje. Usa when con condiciones.
fun level(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): String =
    TODO("Proyecto")

// Devuelve o si es válida; si no, lanza IllegalArgumentException (require) con un mensaje que diga qué falla:
// código en blanco, descripción en blanco, cantidad negativa o medida negativa/NaN.
fun validated(o: Observation): Observation = TODO("Proyecto")

// Una línea "CODE;descripción;cantidad;medida" -> Observation, o null si es inválida:
// no tiene 4 partes, cantidad o medida no numéricas, o no pasa validated.
// Usa split, trim, toIntOrNull, toDoubleOrNull y runCatching/try para validated.
fun parseObservation(line: String): Observation? = TODO("Proyecto")

// Las observaciones válidas de las líneas, en el mismo orden. Un for y una lista mutable.
fun parseValid(lines: List<String>): List<Observation> = TODO("Proyecto")

// Cuántas líneas no son válidas.
fun countInvalid(lines: List<String>): Int = TODO("Proyecto")

// Cabecera, una fila por observación válida con su nivel al final (row + " " + level(measure)),
// y una última línea "Descartadas: N". Si no hay líneas válidas, escribe además "(sin observaciones)".
fun summary(domain: String, lines: List<String>): String = TODO("Proyecto")

sealed interface ParseResult {
    data class Valid(val observation: Observation) : ParseResult
    data class Invalid(val line: String, val reason: String) : ParseResult
}

fun parseLine(line: String): ParseResult {
    val fields = line.split(";").map { it.trim() }
    if (fields.size != 4) return ParseResult.Invalid(line, "se esperaban 4 campos y hay ${fields.size}")
    val quantity = fields[2].toIntOrNull()
        ?: return ParseResult.Invalid(line, "cantidad no numérica: '${fields[2]}'")
    val measure = fields[3].toDoubleOrNull()
        ?: return ParseResult.Invalid(line, "medida no numérica: '${fields[3]}'")
    return try {
        ParseResult.Valid(Observation(Code(fields[0]), fields[1], quantity, measure))
    } catch (e: IllegalArgumentException) {
        ParseResult.Invalid(line, e.message ?: "línea inválida")
    }
}

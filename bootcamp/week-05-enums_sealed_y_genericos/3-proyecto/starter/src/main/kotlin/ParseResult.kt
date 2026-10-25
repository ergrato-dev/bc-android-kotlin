// Una línea leída es o válida o inválida, y si es inválida sabemos POR QUÉ.
sealed interface ParseResult {
    data class Valid(val observation: Observation) : ParseResult
    data class Invalid(val line: String, val reason: String) : ParseResult
}

// Paso 4: "CODE;descripción;cantidad;medida". Los motivos, exactos:
//   no son 4 campos          -> "se esperaban 4 campos y hay N"
//   cantidad no es un entero -> "cantidad no numérica: 'X'"      (X es el texto recortado)
//   medida no es un número   -> "medida no numérica: 'X'"
//   Code u Observation lanza -> el mensaje de la IllegalArgumentException
// Recorta los espacios de cada campo. No dejes escapar ninguna excepción.
fun parseLine(line: String): ParseResult = TODO("Paso 4")

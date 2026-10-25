data class InvalidItem(val index: Int, val reason: String)

sealed interface Imported {
    data class Ok(val domain: String, val observations: List<Observation>, val invalid: List<InvalidItem>) : Imported

    data class Rejected(val reason: String) : Imported
}

// Paso 4: importFile NO lanza nunca. Es TOLERANTE: un archivo con observaciones rotas importa las buenas y cuenta
// las malas. Trabaja con el árbol (parseToJsonElement) y decodifica cada observación por separado
// (decodeFromJsonElement). Reglas, con motivos EXACTOS:
//   - el texto no es JSON, o la raíz no es un objeto                      -> Rejected("archivo inválido")
//   - "version" ausente vale 1; presente pero no entero                   -> Rejected("archivo inválido")
//   - version > FORMAT_VERSION                                            -> Rejected("versión no soportada: N")
//   - "domain" ausente o que no es un texto, o "observations" que no es un array -> Rejected("archivo inválido")
//   - Cada elemento de "observations" que falle va a invalid con su POSICIÓN (empezando en 0) y un motivo:
//       falta un campo obligatorio (MissingFieldException)                -> "campo obligatorio ausente"
//       el init del modelo lanza (viene como causa del error)             -> el mensaje de esa causa
//                                                                            ("description en blanco", "code en blanco"...)
//       cualquier otro SerializationException (tipo equivocado...)        -> "valor no válido"
//   - Las claves que no conoce se ignoran; "counts" no se lee.
// El orden de `observations` y de `invalid` es el del archivo.
// MissingFieldException es experimental: @OptIn(ExperimentalSerializationApi::class) en la función.
fun importFile(text: String): Imported = TODO("Paso 4")

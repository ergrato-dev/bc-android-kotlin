// Paso 6: construye un archivo con el DSL con datos de tu dominio, escríbelo con sangría, vuelve a importarlo y,
// además, importa un texto con varias observaciones rotas (motivos distintos) y una versión futura.
fun main() {
    val file = logbookFile("MI-DOMINIO") {
        observation("OBS-001") { description = "Ejemplo uno"; quantity = 3; measure = 12.5 }
        observation("OBS-002") { description = "Ejemplo dos"; measure = 57.25 }
    }
    println(encodeFile(file, pretty = true))
    println(importFile(encodeFile(file)))

    val dirty = """
        {"domain":"MI-DOMINIO","observations":[
          {"code":"OBS-010","description":"Bien","quantity":1,"measure":2.0},
          {"code":"OBS-011","quantity":1,"measure":2.0},
          {"code":"OBS-012","description":" ","quantity":1,"measure":2.0},
          "esto no es una observación"
        ]}
    """.trimIndent()
    println(importFile(dirty))
    println(importFile("""{"version":2,"domain":"x","observations":[]}"""))
}

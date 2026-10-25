@DslMarker
annotation class LogbookDsl

// Paso 5: un DSL que construye un LogbookFile.
//   val file = logbookFile("agro") {
//       observation("A-1") { description = "Riego"; quantity = 3; measure = 12.5 }
//       observation("A-2") { description = "Plaga"; measure = 4.0 }
//   }
// - quantity vale 1 y measure 0.0 por defecto; description es obligatoria: IllegalStateException("description obligatoria en A-1").
// - Un code repetido: IllegalArgumentException("code repetido: A-1"). Un domain en blanco: IllegalArgumentException("domain en blanco").
// - counts se calcula SIEMPRE con los tres niveles (Level.entries.associateWith); version vale FORMAT_VERSION.
// - Marca los dos constructores con @LogbookDsl.
// Ojo: dentro del miembro `observation`, escribir `observation(...)` se llama a SÍ MISMO; construye con ObservationBuilder.
class ObservationBuilder(private val code: String) {
    var description: String = ""
    var quantity: Int = 1
    var measure: Double = 0.0

    fun build(): Observation = TODO("Paso 5")
}

class LogbookFileBuilder(private val domain: String) {
    fun observation(code: String, block: ObservationBuilder.() -> Unit) {
        TODO("Paso 5")
    }

    fun build(): LogbookFile = TODO("Paso 5")
}

fun logbookFile(domain: String, block: LogbookFileBuilder.() -> Unit): LogbookFile = TODO("Paso 5")

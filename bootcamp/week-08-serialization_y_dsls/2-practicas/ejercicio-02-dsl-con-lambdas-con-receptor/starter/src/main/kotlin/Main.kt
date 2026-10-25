// Marca los constructores de un mismo DSL: dentro de un bloque, un `observation(...)` NO puede resolverse hacia
// el constructor exterior sin decirlo (paso 7, NOTAS.md).
@DslMarker
annotation class LogbookDsl

// Paso 1: un constructor con valores por defecto. Se usa así:
//   observation("A-1") { description = "Riego norte"; quantity = 3; measure = 12.5 }
// quantity vale 1 y measure 0.0 por defecto; description es OBLIGATORIA: si no se asigna,
// IllegalStateException("description obligatoria en A-1"). Marca la clase con @LogbookDsl.
class ObservationBuilder(private val code: String) {
    var description: String = ""
    var quantity: Int = 1
    var measure: Double = 0.0

    fun build(): Observation = TODO("Paso 1")
}

fun observation(code: String, block: ObservationBuilder.() -> Unit): Observation = TODO("Paso 1")

// Paso 2: logbook("agro") { observation("A-1") { ... }; observation("A-2") { ... } }
// Conserva el orden. Un code repetido lanza IllegalArgumentException("code repetido: A-1"). Marca la clase con
// @LogbookDsl. Un bloque vacío da un Logbook sin observaciones.
// Ojo: dentro del miembro `observation`, escribir `observation(code, block)` se llama a SÍ MISMO (el miembro gana al
// de nivel superior). Construye con ObservationBuilder(code).apply(block).build().
class LogbookBuilder(private val domain: String) {
    fun observation(code: String, block: ObservationBuilder.() -> Unit) {
        TODO("Paso 2")
    }

    fun build(): Logbook = TODO("Paso 2")
}

fun logbook(domain: String, block: LogbookBuilder.() -> Unit): Logbook = TODO("Paso 2")

// Paso 3: un DSL para construir un filtro. Todas las condiciones se combinan con Y; sin condiciones, pasan todas.
//   val f = filter { level(Level.HIGH); measureAtLeast(60.0); describedAs("riego") }
// describedAs: la descripción CONTIENE el texto, sin distinguir mayúsculas.
class FilterBuilder {
    fun level(level: Level) {
        TODO("Paso 3")
    }

    fun measureAtLeast(min: Double) {
        TODO("Paso 3")
    }

    fun describedAs(text: String) {
        TODO("Paso 3")
    }

    fun build(): (Observation) -> Boolean = TODO("Paso 3")
}

fun filter(block: FilterBuilder.() -> Unit): (Observation) -> Boolean = TODO("Paso 3")

// Paso 4: un DSL de informe con secciones. Dentro de una sección, `+"texto"` añade una línea (unaryPlus de String).
//   report {
//       title("Visitas")
//       section("Altas") { +"uno"; line("dos") }
//   }
// Resultado EXACTO (líneas unidas con \n, sin \n final; una línea en blanco antes de cada sección):
//   # Visitas
//
//   ## Altas
//   - uno
//   - dos
class SectionBuilder {
    fun line(text: String) {
        TODO("Paso 4")
    }

    operator fun String.unaryPlus() {
        TODO("Paso 4")
    }

    fun lines(): List<String> = TODO("Paso 4")
}

class ReportBuilder {
    fun title(text: String) {
        TODO("Paso 4")
    }

    fun section(name: String, block: SectionBuilder.() -> Unit) {
        TODO("Paso 4")
    }

    fun build(): String = TODO("Paso 4")
}

fun report(block: ReportBuilder.() -> Unit): String = TODO("Paso 4")

// Paso 5: operadores sobre Logbook.
//   logbook["A-1"]   -> la observación con ese code, o null          (operator get)
//   "A-1" in logbook -> true si existe                                (operator contains)
//   a + b            -> un Logbook nuevo con las de a y luego las de b; si el domain es distinto,
//                       IllegalArgumentException("domains distintos: x e y"); si un code se repite, gana el de a
operator fun Logbook.get(code: String): Observation? = TODO("Paso 5")

operator fun Logbook.contains(code: String): Boolean = TODO("Paso 5")

operator fun Logbook.plus(other: Logbook): Logbook = TODO("Paso 5")

// Paso 6: el resumen como JSON con buildJsonObject (los DSL de kotlinx.serialization también son lambdas con receptor).
// Orden y formato exactos, compacto:
//   {"domain":"agro","total":2,"levels":{"LOW":1,"MEDIUM":1,"HIGH":0},"codes":["A-1","A-2"]}
// levels SIEMPRE con los tres niveles, en el orden del enum.
fun summaryJson(logbook: Logbook): String = TODO("Paso 6")

fun main() {
    val lb = logbook("agro") {
        observation("A-1") { description = "Riego norte"; quantity = 3; measure = 12.5 }
        observation("A-2") { description = "Plaga sur"; measure = 4.0 }
    }
    println(lb)
}

// Paso 1: un enum con propiedades y métodos.
// isWeekend: SATURDAY y SUNDAY. next(): el día siguiente; después de SUNDAY viene MONDAY (usa entries y ordinal).
enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    val isWeekend: Boolean get() = TODO("Paso 1")

    fun next(): Weekday = TODO("Paso 1")
}

// Paso 2: parse busca por nombre sin distinguir mayúsculas y devuelve null si no existe (valueOf lanzaría).
enum class Priority(val weight: Int) {
    LOW(1), MEDIUM(5), HIGH(10);

    companion object {
        fun parse(text: String): Priority? = TODO("Paso 2")
    }
}

// Paso 3: cuántas tareas hay de cada prioridad. SIEMPRE las tres claves, aunque valgan 0.
// urgency: un when SIN else; si añades una prioridad nueva, el compilador te obliga a tratarla.
// LOW -> "puede esperar", MEDIUM -> "esta semana", HIGH -> "hoy".
fun countByPriority(tasks: List<Priority>): Map<Priority, Int> = TODO("Paso 3")

fun urgency(p: Priority): String = TODO("Paso 3")

// Paso 4: una jerarquía cerrada. area con un when exhaustivo y sin else: Empty vale 0.0.
sealed interface Shape {
    data class Circle(val radius: Double) : Shape
    data class Rect(val width: Double, val height: Double) : Shape
    data object Empty : Shape
}

fun area(s: Shape): Double = TODO("Paso 4")

// Paso 5: los estados de una pantalla. render:
// Loading -> "Cargando...", Success con datos -> "<N> elementos", Success vacío -> "Sin datos", Failure -> "Error: <message>".
sealed interface LoadState {
    data object Loading : LoadState
    data class Success(val data: List<String>) : LoadState
    data class Failure(val message: String) : LoadState
}

fun render(state: LoadState): String = TODO("Paso 5")

fun main() {
    println(Weekday.SUNDAY.next())
    println(Priority.parse("high"))
    println(render(LoadState.Success(listOf("a", "b"))))
}

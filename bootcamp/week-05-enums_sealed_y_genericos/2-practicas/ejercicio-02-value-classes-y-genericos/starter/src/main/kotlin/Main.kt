// Paso 1: tipos que no se confunden. Meters no admite negativos y se suma con +. Email: exactamente una "@",
// y no al principio ni al final. En ambos, require en init con un mensaje.
@JvmInline
value class Meters(val value: Double) {
    init {
        TODO("Paso 1")
    }

    operator fun plus(other: Meters): Meters = TODO("Paso 1")
}

@JvmInline
value class Email(val value: String) {
    init {
        TODO("Paso 1")
    }
}

// Paso 2: una pila genérica. popOrNull y peekOrNull devuelven null si está vacía; la lista es privada.
class Stack<T> {
    private val items = mutableListOf<T>()

    val size: Int get() = TODO("Paso 2")

    fun isEmpty(): Boolean = TODO("Paso 2")

    fun push(item: T) {
        TODO("Paso 2")
    }

    fun popOrNull(): T? = TODO("Paso 2")

    fun peekOrNull(): T? = TODO("Paso 2")
}

// Paso 3: funciones genéricas. firstOrDefault: el primero o el valor por defecto.
// maxOfTwo y clamp exigen T : Comparable<T>. clamp(v, min, max): require(min <= max); v acotado.
fun <T> firstOrDefault(list: List<T>, default: T): T = TODO("Paso 3")

fun <T : Comparable<T>> maxOfTwo(a: T, b: T): T = TODO("Paso 3")

fun <T : Comparable<T>> clamp(value: T, min: T, max: T): T = TODO("Paso 3")

// Paso 4: covarianza. Box<out T> solo PRODUCE T, así que un Box<String> sirve donde se pide un Box<Any>.
// Ya está escrito: léelo y comprueba en el test qué compilaría sin "out".
class Box<out T>(val value: T)

// Paso 5: un resultado genérico con éxito o error. Err es Outcome<Nothing>: encaja con cualquier T.
// map transforma el valor de Ok y deja pasar Err tal cual. parseAge: Ok si es un entero entre 0 y 150; si no, Err con un mensaje.
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val message: String) : Outcome<Nothing>
}

fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = TODO("Paso 5")

fun parseAge(text: String): Outcome<Int> = TODO("Paso 5")

// Paso 6: inline + reified: cuántos elementos de la lista son de tipo T. countOfType<String>() cuenta los String.
inline fun <reified T> List<*>.countOfType(): Int = TODO("Paso 6")

fun main() {
    println(Meters(3.0) + Meters(4.5))
    val s = Stack<Int>()
    s.push(1)
    println(s.popOrNull())
    println(parseAge("42").map { it + 1 })
}

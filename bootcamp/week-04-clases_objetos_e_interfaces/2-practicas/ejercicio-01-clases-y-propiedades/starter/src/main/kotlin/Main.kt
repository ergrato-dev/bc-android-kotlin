// Paso 1: un contador. "value" se lee desde fuera pero solo la clase lo cambia (private set, ya declarado).
// increment suma "by" (debe ser > 0, si no require); reset vuelve a 0; isZero es una propiedad calculada.
class Counter {
    var value: Int = 0
        private set

    val isZero: Boolean get() = TODO("Paso 1")

    fun increment(by: Int = 1) {
        TODO("Paso 1")
    }

    fun reset() {
        TODO("Paso 1")
    }
}

// Paso 2: la temperatura nunca baja del cero absoluto (-273.15). Valida en init Y en el setter de celsius.
// fahrenheit es una propiedad calculada: celsius * 9 / 5 + 32.
class Temperature(initialCelsius: Double) {
    init {
        TODO("Paso 2: validar initialCelsius")
    }

    var celsius: Double = initialCelsius
        set(value) {
            TODO("Paso 2: validar y asignar con field")
        }

    val fahrenheit: Double get() = TODO("Paso 2")
}

// Paso 3: ancho y alto deben ser > 0 (init). area, perimeter e isSquare son propiedades calculadas.
// El constructor secundario ya está escrito: léelo y fíjate en cómo delega con "this(...)".
class Rectangle(val width: Double, val height: Double) {
    constructor(side: Double) : this(side, side)

    init {
        TODO("Paso 3")
    }

    val area: Double get() = TODO("Paso 3")
    val perimeter: Double get() = TODO("Paso 3")
    val isSquare: Boolean get() = TODO("Paso 3")
}

// Paso 4: un punto. plus suma coordenada a coordenada; distanceTo es la distancia euclídea (kotlin.math.sqrt; añade el import).
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point = TODO("Paso 4")

    fun distanceTo(other: Point): Double = TODO("Paso 4")
}

// Paso 5: una lista de reproducción. Por fuera solo se ve una List de solo lectura (una copia).
// add rechaza (require) una canción con el mismo título o con seconds <= 0.
data class Song(val title: String, val seconds: Int)

class Playlist {
    private val _songs = mutableListOf<Song>()

    val songs: List<Song> get() = TODO("Paso 5: devuelve una copia")

    val totalSeconds: Int get() = TODO("Paso 5")

    fun add(song: Song) {
        TODO("Paso 5")
    }
}

fun main() {
    val c = Counter()
    c.increment()
    c.increment(5)
    println("Contador: ${c.value}")
    println("100 °C = ${Temperature(100.0).fahrenheit} °F")
    println("Rectángulo 3x4: ${Rectangle(3.0, 4.0).area}")
    println(Point(1, 2) + Point(3, 4))
}

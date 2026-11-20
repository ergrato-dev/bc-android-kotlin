package dev.bitacora.keep

// Ya está hecho. Un programa pequeño con lo que R8 no puede ver: clases que se cargan por su nombre, escrito en un
// archivo, y campos que se leen por reflexión. Sin R8, ./gradlew run funciona; tras R8, ./gradlew runShrunk, no.
fun main() {
    for (sensor in sensors()) {
        println("${sensor.name}: ${sensor.read()}")
    }
    println(describe(Observation("A-1", "alto", Helper.today())))
}

// Las clases de los sensores, en src/main/resources/sensors.txt: el código no las nombra.
interface Sensor {
    val name: String

    fun read(): Double
}

class Rain : Sensor {
    override val name = "lluvia"

    override fun read() = 12.5
}

class Wind : Sensor {
    override val name = "viento"

    override fun read() = 30.0
}

fun sensors(): List<Sensor> =
    Sensor::class.java.getResource("/sensors.txt")!!.readText().lines().filter { it.isNotBlank() }
        .map { Class.forName(it.trim()).getDeclaredConstructor().newInstance() as Sensor }

class Observation(val code: String, val level: String, val day: String)

// Escribe cada campo con su nombre, como haría una biblioteca de JSON que lee por reflexión.
fun describe(value: Any): String =
    value.javaClass.declaredFields.sortedBy { it.name }.joinToString(", ") { field ->
        field.isAccessible = true
        "${field.name}=${field.get(value)}"
    }

// Lo usa el código de forma directa: R8 lo ve, y puede cambiarle el nombre.
object Helper {
    fun today() = "2026-11-20"
}

// Nadie lo usa: R8 lo quita.
class Unused {
    fun hello() = "hola"
}

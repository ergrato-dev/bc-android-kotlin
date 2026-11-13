package dev.bitacora

// Ya está hecho: una versión de una nota. text = null es una nota borrada (una lápida): hace falta recordar que se
// borró, y cuándo, para que una versión más vieja no la resucite. at, en milisegundos; device, el móvil que la escribió.
data class Version(val id: String, val text: String?, val at: Long, val device: String)

// Paso 2: gana la más nueva. Con la misma hora, el móvil de nombre mayor: la regla da igual mientras todos usen la
// misma, y así una versión nunca se gana a sí misma ni dos se ganan la una a la otra.
fun Version.beats(other: Version): Boolean = false

// Paso 3: lo que sabe un móvil y lo que sabe el servidor, juntos: de cada nota, la versión que gana, ordenadas por id.
// Las lápidas también compiten, y se quedan.
@Suppress("UNUSED_PARAMETER")
fun merge(a: List<Version>, b: List<Version>): List<Version> = a

// Ya está hecho: lo que se ve en la pantalla, sin las lápidas.
fun List<Version>.visible(): Map<String, String> = mapNotNull { v -> v.text?.let { v.id to it } }.toMap()

// Paso 4: el reloj de un móvil: nunca da dos veces la misma hora ni va hacia atrás, aunque el del sistema sí (dos cambios en el
// mismo milisegundo, o la hora corregida por la red).
class Clock(private val system: () -> Long) {
    fun now(): Long = system()
}

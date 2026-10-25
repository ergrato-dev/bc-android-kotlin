// Paso 1: función de extensión: el Double con 2 decimales, con Locale.ROOT (import java.util.Locale). 3.14159.format2() == "3.14".
fun Double.format2(): String = TODO("Paso 1")

// Paso 2: propiedad de extensión: true si el String tiene la forma AAA-000 (tres mayúsculas, guion, tres dígitos).
val String.isCode: Boolean get() = TODO("Paso 2 (Regex o comprobaciones)")

// Paso 3: una interfaz con una implementación por defecto. describe() = "<name> con área <area con 2 decimales>".
interface Shape {
    val name: String
    fun area(): Double
    fun describe(): String = TODO("Paso 3")
}

class Circle(val radius: Double) : Shape {
    override val name: String = "Círculo"
    override fun area(): Double = TODO("Paso 3: PI * r * r (import kotlin.math.PI)")
}

class Square(val side: Double) : Shape {
    override val name: String = "Cuadrado"
    override fun area(): Double = TODO("Paso 3")
}

// Paso 4: funciones sobre cualquier Shape. largest devuelve null si no hay figuras.
fun totalArea(shapes: List<Shape>): Double = TODO("Paso 4")
fun largest(shapes: List<Shape>): Shape? = TODO("Paso 4")

// Paso 5: una jerarquía. Las clases son final por defecto: la base es abstract y title() es open.
// Manager: salary = baseSalary + bonus; title "Gerente".
// Developer: level entre 1 y 5 (require); salary = baseSalary + baseSalary * level / 10; title "Desarrollador nivel <level>".
abstract class Employee(val name: String, val baseSalary: Double) {
    abstract fun salary(): Double
    open fun title(): String = "Empleado"
}

class Manager(name: String, baseSalary: Double, val bonus: Double) : Employee(name, baseSalary) {
    override fun salary(): Double = TODO("Paso 5")
    override fun title(): String = TODO("Paso 5")
}

class Developer(name: String, baseSalary: Double, val level: Int) : Employee(name, baseSalary) {
    init {
        TODO("Paso 5: validar level")
    }

    override fun salary(): Double = TODO("Paso 5")
    override fun title(): String = TODO("Paso 5")
}

// Paso 6: polimorfismo: suma de salary() de todos, sin preguntar por el tipo concreto.
fun payroll(employees: List<Employee>): Double = TODO("Paso 6")

// Paso 7: una versión "mayor.menor.parche" con orden natural (Comparable) y un companion con parse.
// compareTo: mayor, luego menor, luego parche (compareValuesBy). toString: "1.2.3".
// parse: null si no son exactamente 3 enteros no negativos separados por puntos.
data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int = TODO("Paso 7")
    override fun toString(): String = TODO("Paso 7")

    companion object {
        fun parse(text: String): Version? = TODO("Paso 7")
    }
}

fun main() {
    val shapes = listOf(Circle(5.0), Square(3.0))
    shapes.forEach { println(it.describe()) }
    println(listOf(Version(1, 10, 0), Version(1, 2, 0)).sorted())
}

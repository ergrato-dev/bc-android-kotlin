// Base de la semana 5. Si ya tienes tus versiones, úsalas en lugar de estos archivos.
@JvmInline
value class Code(val value: String) : Comparable<Code> {
    init {
        require(value.isNotBlank()) { "code en blanco" }
    }

    override fun compareTo(other: Code): Int = value.compareTo(other.value)

    override fun toString(): String = value
}

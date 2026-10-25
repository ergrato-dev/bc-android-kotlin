// Paso 1: el código deja de ser un String suelto: un Code no se confunde con una descripción.
// init: require no en blanco, mensaje "code en blanco". Orden natural: el del texto.
@JvmInline
value class Code(val value: String) : Comparable<Code> {
    init {
        TODO("Paso 1")
    }

    override fun compareTo(other: Code): Int = TODO("Paso 1")

    override fun toString(): String = value
}

import java.util.Properties

// Ya está hecho: lee build-info.properties, que el plugin bitacora.build-info pone en los recursos.
fun buildInfo(): Properties = Properties().apply {
    val stream = object {}.javaClass.getResourceAsStream("/build-info.properties") ?: error("Falta build-info.properties en los recursos")
    stream.use { load(it) }
}

fun main() {
    val info = buildInfo()
    println("Bitácora ${info.getProperty("domain")} v${info.getProperty("version")}")
    val text = encode(listOf(Observation("A-1", Level.HIGH, "riego")))
    println(text)
    println(decode(text).countByLevel())
}

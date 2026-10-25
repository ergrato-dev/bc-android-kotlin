import java.util.Properties

fun buildInfo(): Properties {
    val stream = checkNotNull(object {}.javaClass.getResourceAsStream("/build-info.properties")) {
        "falta build-info.properties en el classpath"
    }
    return Properties().apply { stream.use { load(it) } }
}

fun main() {
    val info = buildInfo()
    println("Bitácora ${info.getProperty("domain")} v${info.getProperty("version")}")
}

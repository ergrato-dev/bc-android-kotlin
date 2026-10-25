fun main() {
    val text = encode(listOf(Observation("A-1", Level.HIGH, "riego")))
    println(text)
    println(decode(text).countByLevel())
}

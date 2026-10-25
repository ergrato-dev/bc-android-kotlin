// En el JSON los niveles van en minúsculas: "low", "medium", "high" (@SerialName en cada constante).
enum class Level {
    LOW, MEDIUM, HIGH;

    companion object {
        fun of(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): Level {
            require(!measure.isNaN() && measure >= 0) { "measure inválida: $measure" }
            return when {
                measure < mediumFrom -> LOW
                measure < highFrom -> MEDIUM
                else -> HIGH
            }
        }
    }
}

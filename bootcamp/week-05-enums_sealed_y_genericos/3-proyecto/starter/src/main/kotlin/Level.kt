// Paso 2: los niveles dejan de ser Strings. LOW si measure < mediumFrom, MEDIUM si mediumFrom <= measure < highFrom,
// HIGH si measure >= highFrom. Una medida negativa o NaN: require con un mensaje que incluya el valor.
enum class Level {
    LOW, MEDIUM, HIGH;

    companion object {
        fun of(measure: Double, mediumFrom: Double = 10.0, highFrom: Double = 50.0): Level = TODO("Paso 2")
    }
}

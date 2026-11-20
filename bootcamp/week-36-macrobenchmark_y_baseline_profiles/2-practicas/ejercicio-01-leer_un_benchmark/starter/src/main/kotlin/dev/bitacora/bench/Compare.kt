package dev.bitacora.bench

enum class Change { FASTER, SLOWER, NOISE, TOO_NOISY }

// change: cuánto cambia la mediana, en proporción a la de antes (-0,10 es un 10 % menos).
data class Verdict(val change: Double, val kind: Change)

// TODO (paso 5): una medida que varía más que maxNoise (antes o después) no dice nada: TOO_NOISY. Un cambio más
// pequeño que threshold, en valor absoluto, tampoco: NOISE. Si no, FASTER o SLOWER.
fun compare(before: Metric, after: Metric, threshold: Double = 0.05, maxNoise: Double = 0.10): Verdict = TODO()

package dev.bitacora.bench

import java.io.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Ya está hecho: lo que deja Macrobenchmark en <módulo>-benchmarkData.json, solo con lo que usa este ejercicio. El
// archivo tiene más (las trazas, el número de serie del móvil...): ignoreUnknownKeys se lo salta.
@Serializable
data class Results(val context: Context, val benchmarks: List<Benchmark>)

@Serializable
data class Context(val build: Build, val cpuLocked: Boolean)

@Serializable
data class Build(val model: String)

@Serializable
data class Benchmark(
    val name: String,
    val className: String,
    // Un valor por iteración: timeToInitialDisplayMs, timeToFullDisplayMs, frameCount...
    val metrics: Map<String, Metric> = emptyMap(),
    // Muchos valores por iteración: uno por fotograma (frameDurationCpuMs, frameOverrunMs).
    val sampledMetrics: Map<String, SampledMetric> = emptyMap(),
)

@Serializable
data class Metric(
    val minimum: Double,
    val maximum: Double,
    val median: Double,
    val coefficientOfVariation: Double,
    val runs: List<Double>,
)

@Serializable
data class SampledMetric(
    @SerialName("P50") val p50: Double,
    @SerialName("P90") val p90: Double,
    @SerialName("P95") val p95: Double,
    @SerialName("P99") val p99: Double,
    val runs: List<List<Double>>,
)

private val json = Json { ignoreUnknownKeys = true }

fun readResults(file: File): Results = json.decodeFromString(file.readText())

fun Results.benchmark(name: String): Benchmark = benchmarks.single { it.name == name }

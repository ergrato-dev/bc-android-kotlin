package dev.bitacora.bench

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// No los toques: son el enunciado. startup.json y scroll.json son de verdad: los dejó Macrobenchmark al medir la
// Bitácora en un emulador Pixel 6 con API 36.
class BenchTest {
    private val startup = readResults(File("src/test/resources/startup.json"))
    private val scroll = readResults(File("src/test/resources/scroll.json"))

    private fun metric(median: Double, cv: Double) = Metric(median, median, median, cv, listOf(median))

    @Test
    fun itReadsTheEmulatorRun() {
        assertEquals("sdk_gphone64_x86_64", startup.context.build.model)
        assertEquals(false, startup.context.cpuLocked)
        assertEquals(listOf("withoutProfile", "withBaselineProfile", "fullyCompiled"), startup.benchmarks.map { it.name })
    }

    @Test
    fun theMedianOfAnOddList() {
        assertEquals(700.0, median(listOf(720.0, 650.0, 700.0, 1900.0, 690.0)))
    }

    @Test
    fun theMedianOfAnEvenList() {
        assertEquals(695.0, median(listOf(720.0, 650.0, 700.0, 690.0)))
    }

    @Test
    fun theMedianIsTheOneMacrobenchmarkWrites() {
        for (benchmark in startup.benchmarks) {
            for ((name, metric) in benchmark.metrics) {
                assertEquals(metric.median, median(metric.runs), 1e-9, "${benchmark.name} $name")
            }
        }
    }

    // Media 5; desviación típica muestral, raíz de 32 / 7.
    @Test
    fun theCoefficientOfVariationByHand() {
        assertEquals(0.4276, coefficientOfVariation(listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)), 1e-4)
    }

    @Test
    fun theCoefficientOfVariationIsTheOneMacrobenchmarkWrites() {
        for (benchmark in startup.benchmarks) {
            for ((name, metric) in benchmark.metrics) {
                assertEquals(metric.coefficientOfVariation, coefficientOfVariation(metric.runs), 1e-9, "${benchmark.name} $name")
            }
        }
    }

    // Los percentiles de los fotogramas se calculan con los de todas las iteraciones juntos.
    @Test
    fun thePercentilesAreTheOnesMacrobenchmarkWrites() {
        for (benchmark in scroll.benchmarks) {
            for ((name, metric) in benchmark.sampledMetrics) {
                val frames = metric.runs.flatten()
                assertEquals(metric.p50, percentile(frames, 0.50), 1e-9, "${benchmark.name} $name P50")
                assertEquals(metric.p90, percentile(frames, 0.90), 1e-9, "${benchmark.name} $name P90")
                assertEquals(metric.p99, percentile(frames, 0.99), 1e-9, "${benchmark.name} $name P99")
            }
        }
    }

    @Test
    fun aSmallChangeIsNoise() {
        assertEquals(Change.NOISE, compare(metric(700.0, 0.03), metric(680.0, 0.03)).kind)
    }

    @Test
    fun aBigDropIsFaster() {
        val verdict = compare(metric(700.0, 0.03), metric(560.0, 0.04))
        assertEquals(Change.FASTER, verdict.kind)
        assertEquals(-0.20, verdict.change, 1e-9)
    }

    // En el emulador, el arranque hasta la lista varía casi un 12 %: con eso no se puede decir nada.
    @Test
    fun theEmulatorIsTooNoisyForTheFullDisplay() {
        val before = startup.benchmark("withoutProfile").metrics.getValue("timeToFullDisplayMs")
        val after = startup.benchmark("withBaselineProfile").metrics.getValue("timeToFullDisplayMs")
        assertEquals(Change.TOO_NOISY, compare(before, after).kind)
    }

    // Y el primer fotograma, con el Baseline Profile, se mueve menos de un 2 %.
    @Test
    fun theFirstFrameDoesNotChange() {
        val before = startup.benchmark("withoutProfile").metrics.getValue("timeToInitialDisplayMs")
        val after = startup.benchmark("withBaselineProfile").metrics.getValue("timeToInitialDisplayMs")
        val verdict = compare(before, after)
        assertEquals(Change.NOISE, verdict.kind)
        assertEquals(-0.018, verdict.change, 1e-3)
    }
}

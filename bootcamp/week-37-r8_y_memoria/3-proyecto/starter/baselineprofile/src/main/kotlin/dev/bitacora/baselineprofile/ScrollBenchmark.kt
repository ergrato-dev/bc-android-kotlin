package dev.bitacora.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Los fotogramas al recorrer una lista de 40. Sin startupMode: con StartupMode.COLD, Macrobenchmark mata la app al
// terminar setupBlock, y el bloque medido empezaría sin app y sin lista.
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun withoutProfile() = scroll(CompilationMode.None())

    @Test
    fun withBaselineProfile() = scroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun scroll(mode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = mode,
        iterations = 5,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            fill(40)
        },
    ) {
        scrollTheList()
    }
}

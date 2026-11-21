package dev.bitacora.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// El arranque en frío, con tres compilaciones: nada (como recién instalada desde un APK sin perfil), el Baseline Profile
// (como recién instalada desde Play) y todo compilado (el techo). timeToFullDisplayMs sale de ReportDrawnWhen.
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun withoutProfile() = startup(CompilationMode.None())

    @Test
    fun withBaselineProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    @Test
    fun fullyCompiled() = startup(CompilationMode.Full())

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 15,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }
}

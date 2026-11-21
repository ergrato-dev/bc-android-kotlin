package dev.bitacora.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// ./gradlew :app:generateBaselineProfile: ejecuta estos recorridos y guarda las clases y métodos que han hecho falta en
// app/src/fieldRelease/generated/baselineProfiles. En los benchmarks (benchmarkRelease) se saltan.
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    // El arranque, también al perfil de arranque (startup-prof.txt): esas clases van juntas en el primer dex (con R8).
    @Test
    fun startup() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
    }

    // Lo que se hace después de arrancar: añadir y recorrer la lista. Solo al Baseline Profile.
    @Test
    fun addAndScroll() = rule.collect(packageName = PACKAGE) {
        pressHome()
        startActivityAndWait()
        fill(40)
        scrollTheList()
    }
}

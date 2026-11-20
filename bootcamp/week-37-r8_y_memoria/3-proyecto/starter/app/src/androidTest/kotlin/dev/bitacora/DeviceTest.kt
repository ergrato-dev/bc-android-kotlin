package dev.bitacora

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
import dev.bitacora.data.testing.FakeRepository
import dev.bitacora.data.testing.FakeSettings
import javax.inject.Singleton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
object DeviceModule {
    @Provides
    @Singleton
    fun observations(): ObservationRepository = FakeRepository()

    @Provides
    @Singleton
    fun settings(): SettingsRepository = FakeSettings()
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeviceTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    // Semana 36: en el dispositivo, el idioma es el suyo. Con "Descripción" escrito a mano, este test fallaba en un
    // emulador en inglés: los textos salen de los recursos, como en la app.
    @Test
    fun addingShowsTheRow() {
        val high = rule.activity.getString(R.string.level_high)
        rule.onNodeWithText(rule.activity.getString(R.string.description_hint)).performTextInput("riego")
        rule.onNodeWithText(high).performClick()
        rule.onNodeWithText("A-1 · $high · riego").assertExists()
    }

    // Semana 36: timeToFullDisplayMs de Macrobenchmark sale de aquí. En Robolectric, ReportDrawnWhen no llega a
    // reportFullyDrawn: esto solo se puede comprobar en un dispositivo.
    @Test
    fun itReportsFullyDrawn() {
        rule.waitUntil(5_000) { rule.activity.fullyDrawnReporter.isFullyDrawnReported }
    }
}

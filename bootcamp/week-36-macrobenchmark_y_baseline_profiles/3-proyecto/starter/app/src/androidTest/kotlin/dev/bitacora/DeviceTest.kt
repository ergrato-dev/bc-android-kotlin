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

    @Test
    fun addingShowsTheRow() {
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("A-1 · Alta · riego").assertExists()
    }
}

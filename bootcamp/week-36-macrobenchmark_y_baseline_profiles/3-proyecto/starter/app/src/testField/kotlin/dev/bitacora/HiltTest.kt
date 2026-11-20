package dev.bitacora

import dev.bitacora.data.testing.FakeRepository
import dev.bitacora.data.testing.FakeSettings
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.data.ListSettings
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.SettingsRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

// En los tests con @HiltAndroidTest, este módulo ocupa el sitio de AppModule: los repositorios de mentira, sin archivo.
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
object FakeModule {
    @Provides
    @Singleton
    fun observations(): ObservationRepository = FakeRepository(listOf(Observation("A-7", Level.MEDIUM, "del módulo de los tests")))

    @Provides
    @Singleton
    fun settings(): SettingsRepository = FakeSettings()
}

// Semana 29: Hilt crea la actividad, el ViewModel y lo que necesitan. Con HiltTestApplication y la regla, el grafo de
// los tests, con FakeModule.
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, qualifiers = "+es")
class HiltTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createEmptyComposeRule()

    @Inject
    lateinit var repository: ObservationRepository

    @Before
    fun inject() = hilt.inject()

    @Test
    fun theActivityGetsTheTestRepository() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("A-7 · Media · del módulo de los tests").assertExists()
    }

    // @Singleton: el test y la actividad reciben el mismo. Lo que el test pone en él sale en la pantalla.
    @Test
    fun theTestAndTheAppShareTheRepository() {
        Robolectric.buildActivity(MainActivity::class.java).setup()
        (repository as FakeRepository).observations.value = listOf(Observation("A-8", Level.LOW, "puesta por el test"))
        rule.onNodeWithText("A-8 · Baja · puesta por el test").assertExists()
    }

    @Inject
    lateinit var settings: SettingsRepository

    // Paso 9. El SettingsRepository del grafo de test es el que lee la actividad: con "Solo altas", la A-7 (Media)
    // no sale.
    @Test
    fun theTestSettingsReachTheScreen() {
        (settings as FakeSettings).settings.value = ListSettings(onlyHigh = true)
        Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("A-7 · Media · del módulo de los tests").assertDoesNotExist()
    }
}

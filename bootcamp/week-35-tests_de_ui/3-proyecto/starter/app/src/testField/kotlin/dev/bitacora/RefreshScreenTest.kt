package dev.bitacora

import dev.bitacora.data.testing.FakeRepository
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dev.bitacora.network.NetworkError
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.RefreshResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import javax.inject.Inject

// Semana 31: "Actualizar" en la pantalla, con el repositorio de FakeModule. Ni servidor ni red: lo que conteste el
// repositorio es lo que sale en el aviso.
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, qualifiers = "+es")
class RefreshScreenTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createEmptyComposeRule()

    @Inject
    lateinit var repository: ObservationRepository

    private val fake get() = repository as FakeRepository

    @Before
    fun inject() = hilt.inject()

    private fun refresh(answer: RefreshResult) {
        fake.answer.value = answer
        Robolectric.buildActivity(MainActivity::class.java).setup()
        rule.onNodeWithText("Actualizar").performClick()
    }

    @Test
    fun whatArrivedIsCounted() {
        refresh(RefreshResult.Done(2))
        rule.onNodeWithText("Recibidas del servidor: 2").assertExists()
    }

    @Test
    fun withoutNetworkItSaysSo() {
        refresh(RefreshResult.Failed(NetworkError.OFFLINE))
        rule.onNodeWithText("Sin conexión").assertExists()
    }

    @Test
    fun aSlowServerIsNotOffline() {
        refresh(RefreshResult.Failed(NetworkError.TIMEOUT))
        rule.onNodeWithText("El servidor tarda demasiado").assertExists()
    }
}

package dev.bitacora

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.lifecycle.ViewModelProvider
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

// Semana 30: la lista llega de Room, en otro hilo, y waitForIdle solo espera al hilo principal. Esto abre la actividad
// y espera a que el ViewModel tenga la lista.
fun ComposeTestRule.launch(intent: Intent? = null, saved: Bundle? = null): ActivityController<MainActivity> =
    Robolectric.buildActivity(MainActivity::class.java, intent)
        .let { if (saved == null) it.setup() else it.setup(saved) }
        .also { waitForTheList(it.get()) }

fun ComposeTestRule.waitForTheList(activity: MainActivity) {
    val viewModel = ViewModelProvider(activity)[BitacoraViewModel::class.java]
    waitUntil(5_000) {
        // Lo que Room ha dejado para el hilo principal: sin esto, la lista llega pero nadie la recoge.
        shadowOf(Looper.getMainLooper()).idle()
        viewModel.state.value.loaded
    }
}

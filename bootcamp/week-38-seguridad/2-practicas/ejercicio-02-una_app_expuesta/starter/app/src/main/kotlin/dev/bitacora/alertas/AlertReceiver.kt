package dev.bitacora.alertas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Una alerta nueva: su código y su texto, en el Intent.
class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getStringExtra(EXTRA_CODE) ?: return
        context.notifyAlert(code, intent.getStringExtra("text").orEmpty())
    }
}

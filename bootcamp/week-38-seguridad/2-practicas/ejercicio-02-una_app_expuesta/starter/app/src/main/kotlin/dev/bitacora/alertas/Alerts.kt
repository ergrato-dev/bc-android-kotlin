package dev.bitacora.alertas

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.net.HttpURLConnection
import java.net.URL

const val ALERTS_URL = "http://alertas.bitacora.example/v1/alerts"
const val NEW_ALERT = "dev.bitacora.alertas.NEW_ALERT"
const val OPEN_ALERT = "dev.bitacora.alertas.OPEN_ALERT"
const val EXTRA_CODE = "code"
private const val CHANNEL = "alerts"

// Lo que abre la notificación de una alerta: su detalle.
fun openIntent(context: Context, code: String): PendingIntent = PendingIntent.getActivity(
    context,
    code.hashCode(),
    Intent(OPEN_ALERT).putExtra(EXTRA_CODE, code),
    // Desde Android 14, un PendingIntent mutable con un Intent implícito falla, salvo con este flag.
    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT,
)

fun Context.notifyAlert(code: String, text: String) {
    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.alerts), NotificationManager.IMPORTANCE_DEFAULT))
    val notification = Notification.Builder(this, CHANNEL)
        .setSmallIcon(android.R.drawable.stat_notify_error)
        .setContentTitle(code)
        .setContentText(text)
        .setContentIntent(openIntent(this, code))
        .setAutoCancel(true)
        .build()
    manager.notify(code.hashCode(), notification)
}

// Las alertas del servidor, en texto. Bloquea: fuera del hilo principal.
fun Context.fetchAlerts(): String {
    val connection = URL(ALERTS_URL).openConnection() as HttpURLConnection
    connection.setRequestProperty("X-Api-Key", getString(R.string.alerts_key))
    return try {
        connection.inputStream.bufferedReader().readText()
    } finally {
        connection.disconnect()
    }
}

package dev.bitacora.alertas

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.IOException
import kotlin.concurrent.thread

// Sin Compose ni AndroidX: lo que importa esta semana es el manifiesto y lo que sale del APK.
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(POST_NOTIFICATIONS), 0)
        val result = TextView(this)
        val refresh = Button(this).apply {
            setText(R.string.refresh)
            setOnClickListener {
                thread {
                    val text = try { fetchAlerts() } catch (e: IOException) { e.toString() }
                    runOnUiThread { result.text = text }
                }
            }
        }
        // La alerta de prueba pasa por AlertReceiver, como las de verdad.
        val tryAlert = Button(this).apply {
            setText(R.string.try_alert)
            setOnClickListener {
                sendBroadcast(Intent(NEW_ALERT).setPackage(packageName).putExtra(EXTRA_CODE, "A-2").putExtra("text", "Pulgón en la parcela 2"))
            }
        }
        // Con targetSdk 35 o más, la app se dibuja bajo las barras del sistema: esto deja su hueco.
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            addView(refresh)
            addView(tryAlert)
            addView(result)
        })
    }
}

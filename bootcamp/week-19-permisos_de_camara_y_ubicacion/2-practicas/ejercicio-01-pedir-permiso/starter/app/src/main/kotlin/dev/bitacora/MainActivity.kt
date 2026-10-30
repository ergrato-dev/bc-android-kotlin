package dev.bitacora

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    lateinit var status: TextView
        private set
    lateinit var settings: Button
        private set
    // Paso 3: si ya se ha explicado por qué hace falta la ubicación.
    private var explained = false

    // Paso 2: el lanzador de RequestMultiplePermissions. Con alguno concedido, locate(). Con los dos denegados: si
    //         Android aconseja explicar (shouldShowRequestPermissionRationale), R.string.denied; si no, el usuario ha
    //         dicho "no" para siempre: R.string.denied_forever y el botón de ajustes visible.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { setText(R.string.idle) }
        settings = Button(this).apply {
            setText(R.string.settings)
            visibility = View.GONE
            setOnClickListener { openSettings() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(status)
            addView(Button(context).apply {
                setText(R.string.locate)
                setOnClickListener { onLocate() }
            })
            addView(settings)
        })
    }

    // Paso 3: con la ubicación aproximada concedida, locate(). Si Android aconseja explicar y aún no lo has hecho,
    //         R.string.rationale (sin pedir nada). Si no, pide los dos permisos a la vez.
    fun onLocate(): Unit = TODO("Paso 3")

    // Paso 4: comprueba aquí mismo el permiso (ContextCompat.checkSelfPermission), para que lint lo vea. Con la precisa,
    //         el proveedor GPS; solo con la aproximada, el de red. R.string.locating mientras llega, y
    //         LocationManagerCompat.getCurrentLocation, que entrega el resultado a show().
    private fun locate(): Unit = TODO("Paso 4")

    // Ya está hecho: muestra la ubicación, o que no la hay.
    private fun show(location: Location?, precise: Boolean) {
        if (location == null) {
            status.setText(R.string.idle)
            return
        }
        val format = if (precise) R.string.precise else R.string.approximate
        status.text = getString(format, location.latitude.toString(), location.longitude.toString())
    }

    // Ya está hecho: la pantalla de ajustes de esta app, donde el usuario puede volver a conceder el permiso.
    private fun openSettings() =
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
}

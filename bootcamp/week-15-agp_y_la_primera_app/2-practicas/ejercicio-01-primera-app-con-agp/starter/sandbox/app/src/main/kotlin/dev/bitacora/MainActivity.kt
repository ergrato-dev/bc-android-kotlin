package dev.bitacora

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

// Ya está hecho: la pantalla más pequeña posible, un texto. Las interfaces de verdad llegan con Compose (fase 4).
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Bitácora" })
    }
}

package dev.bitacora.help

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.TextView

const val TIPS = 3

// R es el de la biblioteca: dev.bitacora.help.R, con sus textos y solo los suyos.
fun Context.tips(count: Int): String = TODO("paso 2: el plural tips de la biblioteca")

// La pantalla de la ayuda, sin Compose: un título y un texto.
class HelpActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.help_title)
        setContentView(TextView(this).apply { text = getString(R.string.help_body, tips(TIPS)) })
    }
}

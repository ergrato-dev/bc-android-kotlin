package dev.bitacora

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import dev.bitacora.core.greeting
import java.util.Properties

// Ya está hecho: lee build-info.properties de los assets del APK y saluda con la función de core.
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = Properties().apply { assets.open("build-info.properties").use { load(it) } }
        setContentView(TextView(this).apply { text = greeting(info.getProperty("domain"), info.getProperty("version")) })
    }
}

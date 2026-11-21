package dev.bitacora.alertas

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class DetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = intent.getStringExtra(EXTRA_CODE) })
    }
}

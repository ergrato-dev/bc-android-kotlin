package dev.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity

// La dirección a la que van los partes.
const val REPORTS = "campo@bitacora.example"

// Un parte de campo con vistas: una nota, una foto opcional y "Enviar", que lo manda por correo.
//
// TODO paso 2: "Enviar" empieza apagado y solo se activa con texto que no sea solo espacios. Con
// note.doAfterTextChanged { ... } (de core-ktx, que llega con activity).
// TODO paso 3: "Enviar" abre el correo con Intent.ACTION_SENDTO, data mailto:REPORTS, EXTRA_SUBJECT con
// R.string.subject y EXTRA_TEXT con la nota sin espacios a los lados. "mailto:$REPORTS".toUri() hace la Uri.
// TODO paso 4: "Foto" pide una foto a la cámara con registerForActivityResult(TakePicturePreview()). Con foto, la
// enseña en preview (visible) y status dice R.string.with_photo; sin ella (se cancela), todo sigue igual.
class ReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
    }
}

package dev.bitacora

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// Una valoración de 1 a 5: el estado de una parcela, por ejemplo. Son cinco círculos dibujados con Canvas, y un toque
// en el círculo n la deja en n. Debajo no hay un Button ni un Slider: Compose no sabe qué es, y para TalkBack (y para
// los tests) es una caja vacía.
//
// TODO paso 2: la semántica, en el Modifier del Canvas.
//   - contentDescription: label, para que se sepa de qué es la valoración;
//   - stateDescription: "3 de 5", lo que lee TalkBack después del nombre;
//   - progressBarRangeInfo: el valor, el rango 1f..5f y los pasos que hay entre los extremos (3: 2, 3 y 4);
//   - setProgress: lo que hace TalkBack con los gestos de subir y bajar. Recibe un Float: redondéalo, déjalo entre 1 y
//     5, llama a onValueChange y devuelve true.
@Composable
fun Rating(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier
            .testTag("rating")
            .size(width = 200.dp, height = 40.dp)
            .pointerInput(onValueChange) {
                detectTapGestures { offset -> onValueChange((offset.x / (size.width / 5f)).toInt().coerceIn(0, 4) + 1) }
            },
    ) {
        val side = size.width / 5
        for (i in 0 until 5) {
            val center = Offset(side * i + side / 2, size.height / 2)
            drawCircle(color, radius = side * 0.35f, center = center, style = if (i < value) Fill else Stroke(4f))
        }
    }
}

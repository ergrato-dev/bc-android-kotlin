package dev.bitacora.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

// La app que se mide: field, sin sufijo, en su variante benchmarkRelease.
const val PACKAGE = "dev.bitacora.agro"

private val high = Pattern.compile("Alta|High")

private fun summary(count: Int) = By.text(Pattern.compile("$count .*"))

// Lo que hace una persona, con UiAutomator. Los textos, en español o en inglés: los del idioma del móvil.
// Añade observaciones hasta tener `count`: los datos de la app se quedan entre una iteración y la siguiente.
fun MacrobenchmarkScope.fill(count: Int) {
    device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 5_000)
    repeat(count) { i ->
        if (device.hasObject(summary(count))) return
        device.findObject(By.clazz("android.widget.EditText")).text = "riego ${i + 1}"
        // El que se apaga y se enciende es el botón (la View que se pulsa), no su texto.
        device.wait(Until.findObject(By.clickable(true).enabled(true).hasChild(By.text(high))), 2_000).click()
    }
}

// La lista se busca por su tag: testTagsAsResourceId lo convierte en resource-id (semana 35). Vacía, no está en el
// árbol de UiAutomator.
fun MacrobenchmarkScope.scrollTheList() {
    val list = device.findObject(By.res("observations"))
    // Sin margen, el gesto empieza en el borde y Android lo toma por "atrás".
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
}

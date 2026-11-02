package dev.bitacora

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Paso 3: el tema de la Bitácora. Con dynamicColor y desde Android 12 (API 31), los colores del fondo de pantalla
// (dynamicLightColorScheme / dynamicDarkColorScheme); si no, los de la marca (Color.kt). Claro u oscuro según darkTheme.
@Composable
fun BitacoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(content = content)
}

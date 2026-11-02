package dev.bitacora

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Paso 1: con dynamicColor y desde Android 12 (API 31), los colores del fondo de pantalla (dynamicLightColorScheme o
// dynamicDarkColorScheme); si no, los de la marca (LightColors o DarkColors, en Color.kt). Claro u oscuro según
// darkTheme, que por defecto es lo que diga el sistema.
@Composable
fun BitacoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(content = content)
}

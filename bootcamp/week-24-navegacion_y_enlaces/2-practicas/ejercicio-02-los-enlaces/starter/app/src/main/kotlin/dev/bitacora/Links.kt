package dev.bitacora

import android.content.Intent
import androidx.navigation3.runtime.NavKey

// Paso 2: un UriDeepLinkMatcher con el patrón "bitacora://observation/{code}" y ObservationKey.serializer(), y
// withBackStack para que debajo de la observación quede la sesión.
// Paso 3: el back stack con que empieza la app. Desde el lanzador (un Intent sin Uri), la sesión. Con un enlace que
// encaja y un código que existe, la sesión y la observación. Con cualquier otro enlace, la sesión: viene de fuera.
fun startOf(intent: Intent, exists: (String) -> Boolean): List<NavKey> = listOf(SessionKey)

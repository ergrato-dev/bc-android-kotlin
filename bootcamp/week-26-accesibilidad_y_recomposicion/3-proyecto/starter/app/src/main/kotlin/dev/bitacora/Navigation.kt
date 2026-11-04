package dev.bitacora

import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.deeplink.withBackStack
import kotlinx.serialization.Serializable

// Los destinos de la Bitácora. Una clave lleva lo justo para encontrar lo que enseña: el código, no la observación.
@Serializable
data object SessionKey : NavKey

@Serializable
data class ObservationKey(val code: String) : NavKey

// bitacora://observation/A-2 abre esa observación, con la sesión debajo: atrás vuelve a la lista.
private val observationLink = UriDeepLinkMatcher("bitacora://observation/{code}".toUri(), ObservationKey.serializer())
    .withBackStack { listOf(SessionKey, it.key) }

// Desde el lanzador, la sesión. Un enlace viene de fuera: si no encaja, o el código no existe, también la sesión. El
// Intent del lanzador no tiene Uri, y DeepLinkRequest no admite uno sin Uri ni extras.
fun startOf(intent: Intent, exists: (String) -> Boolean): List<NavKey> {
    if (intent.data == null) return listOf(SessionKey)
    val match = observationLink.match(DeepLinkRequest(intent))
    return match?.backStack?.takeIf { exists(match.key.code) } ?: listOf(SessionKey)
}

package dev.bitacora

import androidx.compose.runtime.Composable

// Paso 1: las claves de los dos destinos, @Serializable y NavKey: SessionKey (un data object) y ObservationKey, con el
// código de la observación.

// Paso 2: un NavDisplay con el back stack de rememberNavBackStack, que empieza en la sesión. Abrir una observación
// añade su clave; "Siguiente" añade la de la observación siguiente (la primera, después de la última). Elegir un nivel
// la guarda en la sesión y quita la clave. Atrás quita la última.
// Paso 3: el estado de cada destino (la nota) se guarda con rememberSaveableStateHolderNavEntryDecorator.
@Composable
fun BitacoraNav(session: Session) {
}

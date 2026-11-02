package dev.bitacora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// La lista, y abajo el SnackbarHost, que pinta el aviso que se le pida a snackbar.
// Paso 3: al borrar una fila, la sesión la quita (remove) y se enseña "A-2 borrada" con "Deshacer", en una corrutina
// (rememberCoroutineScope) y con SnackbarDuration.Long. Si el usuario pulsa "Deshacer", restore la devuelve a su sitio.
// Paso 4: cada fila, con Modifier.animateItem(): las de debajo suben poco a poco.
@Composable
fun SessionScreen(session: Session) {
    val observations by session.observations.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    Surface(Modifier.fillMaxSize()) {
        Box {
            LazyColumn(Modifier.padding(16.dp)) {
                items(observations, key = { it.code }) { observation ->
                    DeletableRow(observation, onDelete = {})
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
}

# Intents y el reducer

> Lo que pasa en la sesión es un número cerrado de cosas: escribir, ubicar, añadir, borrar, deshacer, cambiar el nivel, compartir. En MVI cada una es un tipo, y una sola función, el reducer, dice qué estado sale de cada una. Como el reducer no toca Android ni el disco, sus reglas se prueban con JUnit en milisegundos.

## 🎯 Objetivos

- Escribir los intents como una `sealed interface`
- Escribir un reducer puro: `(estado, intent) -> estado nuevo (+ efecto)`
- Aprovechar que el compilador avisa si falta un caso
- Saber qué no puede hacer un reducer, y quién lo hace

## 1. Los intents

```kotlin
sealed interface BitacoraIntent {
    data class EditDraft(val text: String) : BitacoraIntent
    data object StartLocating : BitacoraIntent
    data object LocationDenied : BitacoraIntent
    data class Located(val position: Position?) : BitacoraIntent
    data class Add(val level: Level) : BitacoraIntent
    data class Delete(val code: String) : BitacoraIntent
    data object Undo : BitacoraIntent
    data class Replace(val observation: Observation) : BitacoraIntent
    data object Share : BitacoraIntent
}
```

Un intent es lo que **ha pasado**, no lo que hay que hacer: `Delete("A-1")`, no `RemoveFromListAndShowSnackbar`. Lleva lo justo: el código, no la observación entera (como las claves de navegación de la semana 24). Los que no llevan nada son `data object`.

No todos vienen de la persona: `Located` llega del `LocationManager`, a través de la actividad. Para el reducer es igual: algo ha pasado.

## 2. El reducer

```kotlin
data class Next(val state: BitacoraUiState, val effect: BitacoraEffect? = null)

fun reduce(state: BitacoraUiState, intent: BitacoraIntent): Next = when (intent) {
    is BitacoraIntent.EditDraft -> Next(state.copy(draft = intent.text))
    is BitacoraIntent.Delete -> {
        val at = state.observations.indexOfFirst { it.code == intent.code }
        if (at < 0) Next(state)
        else Next(
            state.copy(observations = state.observations.filterIndexed { i, _ -> i != at }, deleted = Deleted(state.observations[at], at)),
            BitacoraEffect.ShowUndo(intent.code),
        )
    }
    // ...
}
```

Un reducer es una **función pura**: con el mismo estado y el mismo intent devuelve siempre lo mismo, y no hace nada más. No lee la hora, no escribe en disco, no lanza corrutinas, no conoce `Context`. Eso tiene tres consecuencias:

- **Se prueba sin nada**: un estado de partida, un intent, y se compara lo que sale.
- **Se lee de una vez**: todas las reglas de la sesión están en un `when`.
- **Se puede repetir**: aplicar la misma lista de intents da el mismo estado. Es lo que permite, por ejemplo, reproducir un error con los intents que lo causaron.

> 🔬 Medido en la Bitácora: los 6 tests de `ReducerTest` tardan 2 ms en total, sin `@RunWith`. Los 3 de `ArchitectureTest`, que levantan la actividad con Robolectric, 1,8 s. Lo que se puede probar en el reducer, mejor ahí.

## 3. El compilador cuenta los casos

Con una `sealed interface`, el compilador conoce todos los intents. Un `when` usado como valor tiene que cubrirlos:

> 🔬 Medido con Kotlin 2.4.21, quitando un caso del reducer del ejercicio 01: `'when' expression must be exhaustive. Add the 'ClearDone' branch or an 'else' branch.`

Por eso el reducer **no lleva `else`**. Con `else`, un intent nuevo compila sin que nadie decida qué hace; sin él, el compilador te lleva al sitio.

## 4. Lo que no cambia, tal cual

Cuando un intent no cambia nada (borrar un código que no existe, añadir con el borrador vacío), el reducer devuelve **el mismo** estado, no un `copy()` igual:

```kotlin
if (at < 0) Next(state)
```

Para `StateFlow` da igual (compara con `equals` y no emite un valor igual), pero para el ViewModel no: decide si guarda comparando la lista con `!==`. Una lista nueva igual a la de antes se guardaría para nada. Y Compose, con clases estables, se salta lo que no ha cambiado (semana 26).

## 5. Lo que el reducer no hace

El reducer decide; no ejecuta. Lo que necesita el mundo exterior lo hace el ViewModel con lo que el reducer devuelve:

| Lo que hace falta | Quién |
| --- | --- |
| El estado nuevo | `reduce` |
| Guardar la lista | El ViewModel, si la lista es otra: `repository.save` |
| Avisar "A-1 borrada" | `reduce` lo pide (`ShowUndo`); la actividad lo enseña |
| Pedir el permiso, ubicar | La actividad; cuando llega, `onIntent(Located(...))` |
| El código de la siguiente | `nextCode()` de `core`: también puro, se llama desde `reduce` |

Si una regla necesita algo que no es puro (la hora de la observación, por ejemplo), se mete en el intent: `Add(level, at = Clock.now())`, y el reducer sigue siendo puro.

## Para recordar

- Un intent es algo que ha pasado, con lo justo; los intents son una `sealed interface`.
- El reducer es puro: estado + intent dan estado nuevo y, como mucho, un efecto.
- Sin `else` en el `when`: el compilador avisa del intent que falta.
- Sin cambios, el mismo estado; guardar y avisar lo hace quien llama al reducer.

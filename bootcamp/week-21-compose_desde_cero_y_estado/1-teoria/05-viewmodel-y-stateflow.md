# ViewModel y StateFlow

> `rememberSaveable` sirve para el estado de la pantalla. Las observaciones de la Bitácora son otra cosa: son los datos de la app, los usa la lógica (numerar, contar, compartir) y no son de ningún composable. Su sitio es el `ViewModel` de la semana 16, que ahora las publica como un `StateFlow` de la semana 7. La pantalla lo recoge y lo pinta; los eventos vuelven al `ViewModel` como llamadas a sus funciones.

## 🎯 Objetivos

- Exponer el estado de la pantalla como un `StateFlow<UiState>` desde el `SavedStateHandle`
- Recogerlo en Compose con `collectAsStateWithLifecycle`
- Saber que `stateIn` es asíncrono y qué consecuencias tiene en las acciones y en los tests

## 1. Un estado para la pantalla

La pantalla lee un solo objeto con todo lo que pinta:

```kotlin
data class DraftUiState(val text: String = "", val level: Level? = null, val saved: List<String> = emptyList())
```

El `ViewModel` lo publica como un `StateFlow`, que siempre tiene un valor y avisa cuando cambia ([semana 7](../../week-07-flow_y_programacion_reactiva/README.md)). Para que sobreviva a la muerte del proceso, cada dato vive en el `SavedStateHandle`, y `getStateFlow` da un `StateFlow` por clave:

```kotlin
class DraftViewModel(private val handle: SavedStateHandle) : ViewModel() {
    val state: StateFlow<DraftUiState> = combine(
        handle.getStateFlow("text", ""),
        handle.getStateFlow<Level?>("level", null),
        handle.getStateFlow("saved", emptyList<String>()),
        ::DraftUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftUiState())

    fun onTextChange(text: String) {
        handle["text"] = text.take(MAX_LENGTH)
    }
    // ...
}
```

- `combine` junta los tres flujos y crea un `DraftUiState` cada vez que cambia uno.
- `stateIn` convierte el resultado en un `StateFlow`, con un valor inicial y un ámbito, `viewModelScope`, que se cancela cuando el `ViewModel` muere.
- `WhileSubscribed(5_000)` recoge los flujos de arriba mientras alguien recoja `state`, y hasta 5 segundos después del último. Un giro tarda menos: la actividad nueva vuelve a recoger y el flujo no se reinicia.

Escribir en el handle (`handle["text"] = ...`) actualiza su `StateFlow`, y el cambio llega a `state`.

## 2. stateIn es asíncrono

`state` no se calcula cuando lo lees: lo calcula una corrutina en `viewModelScope`, que corre en el hilo principal. Si nadie lo recoge, ni siquiera corre:

> 🔬 Medido en la sonda, con Robolectric: `onDraft("riego")` y justo después `state.value.draft`. Con `Eagerly`, da `""`; tras `shadowOf(Looper.getMainLooper()).idle()`, `"riego"`. Con `WhileSubscribed` y nadie recogiendo, da `""` también tras `idle()`.

De ahí sale una regla: **las acciones leen del handle, no de `state`**. `state` es lo que la pantalla ve; el handle es lo que hay.

```kotlin
fun save() {
    val text = handle.get<String>("text").orEmpty().trim()
    if (text.isEmpty() || handle.get<Level>("level") == null) return
    handle["saved"] = handle.get<List<String>>("saved").orEmpty() + text
    handle["text"] = ""
    handle["level"] = null
}
```

> 🔬 Medido en el ejercicio 02 (paso 6b): con `save` leyendo de `state.value`, falla solo `onlyACompleteDraftIsSaved`, el test que usa el `ViewModel` sin pantalla. Con la pantalla delante, alguien recoge `state` y el valor está al día; sin ella, `save` ve el `DraftUiState()` inicial y no guarda nada.

En los tests pasa lo mismo: sin pantalla, comprueba el handle (`handle.get<String>("text")`); con pantalla, comprueba lo que se ve. La regla de Compose espera a que el hilo principal termine antes de comprobar.

## 3. La pantalla y la ruta

La pantalla sin estado no sabe que hay un `ViewModel` ([teoría 04](04-el-estado-baja-los-eventos-suben.md)):

```kotlin
@Composable
fun DraftScreen(state: DraftUiState, onTextChange: (String) -> Unit, onLevelChange: (Level) -> Unit, onSave: () -> Unit)
```

Un composable de más arriba, la ruta, los une:

```kotlin
@Composable
fun DraftRoute(viewModel: DraftViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DraftScreen(state, viewModel::onTextChange, viewModel::onLevelChange, viewModel::save)
}
```

- `viewModel()` (de `lifecycle-viewmodel-compose`) pide el `ViewModel` al dueño más cercano, aquí la actividad. Es el mismo que daría `by viewModels()` en la actividad, con su `SavedStateHandle`.
- `collectAsStateWithLifecycle()` (de `lifecycle-runtime-compose`) convierte el `StateFlow` en un `State` de Compose, y solo recoge mientras la actividad está al menos en `STARTED`. `collectAsState()` recogería también con la app detrás, sin que nadie mire. Es el `repeatOnLifecycle` de la [semana 16](../../week-16-actividades_y_ciclo_de_vida/1-teoria/04-observar-el-ciclo-de-vida.md), hecho para Compose.
- El parámetro con valor por defecto deja pasar otro `ViewModel` en un test, por ejemplo uno con un `SavedStateHandle` hecho a mano.

> 🔬 Medido en el ejercicio 02: tras `recreate()`, `ViewModelProvider(activity)[DraftViewModel::class.java]` es el mismo objeto (`assertSame`), y la pantalla nueva enseña el borrador. Tras matar el proceso, el `ViewModel` es otro, y el borrador vuelve porque estaba en el handle.

El `TextField` va bien con el texto en el `ViewModel` mientras la actualización sea inmediata: `onValueChange` escribe en el handle en el mismo hilo, sin esperar a nada. Si entre la tecla y el estado hubiera una espera (un `delay`, una base de datos), el cursor saltaría.

## 4. ¿rememberSaveable o ViewModel?

| | `rememberSaveable` | `ViewModel` + `SavedStateHandle` |
| --- | --- | --- |
| Para qué | Estado de la pantalla: qué está abierto, qué está marcado | Datos de la app y la lógica que los cambia |
| Quién lo usa | El composable y sus hijos | Varias pantallas, la actividad, la lógica |
| Se prueba | Con la regla de Compose | Sin pantalla, con un `SavedStateHandle` hecho a mano |

Los dos sobreviven al giro y a la muerte del proceso. Ninguno es una base de datos: si el usuario cierra la app, o se reinicia el móvil, se pierden. Guardar de verdad es la fase 5.

## ✅ Comprueba

- [ ] ¿Por qué `save` lee del handle y no de `state.value`? ¿En qué test se nota?
- [ ] ¿Qué hace `WhileSubscribed(5_000)` durante un giro? ¿Y cuando el usuario se va de la app?
- [ ] ¿Por qué `DraftRoute` recibe el `ViewModel` como parámetro con valor por defecto?

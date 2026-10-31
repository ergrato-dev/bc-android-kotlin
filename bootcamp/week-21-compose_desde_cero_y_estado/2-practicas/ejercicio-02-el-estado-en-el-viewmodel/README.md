# Ejercicio 02 — El estado en el ViewModel

⏱️ 2 h · Teoría previa: 04 y 05 · Haz antes el ejercicio 01

## Objetivo

El borrador del ejercicio 01, con el estado fuera de la composición. Lo guarda un `ViewModel` en su `SavedStateHandle` y lo publica como un `StateFlow`. La pantalla no tiene estado: la pinta un composable que no sabe que hay un `ViewModel`, y otro, el de la ruta, los une. Ahora la pantalla también enseña la lista de descripciones guardadas.

## Cómo está montado

`starter/` es la app del ejercicio 01 con `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`. `MainActivity` hace `setContent { DraftRoute() }`. En `Draft.kt` están ya `DraftUiState` (lo que pinta la pantalla) y el `LevelPicker` del ejercicio 01; `DraftScreen` y `DraftRoute` están vacíos. `DraftViewModel.kt` tiene la clase con un estado que no cambia nunca y tres funciones vacías.

Los tests (`app/src/test/kotlin/dev/bitacora/DraftViewModelTest.kt`, no los toques) prueban la pantalla sola, la actividad con su `ViewModel`, la muerte del proceso y el `ViewModel` con un `SavedStateHandle` hecho a mano. Al empezar, los 9 fallan.

## Pasos

1. **El estado** (`DraftViewModel`): un `StateFlow` por clave del handle con `handle.getStateFlow("text", "")`, `getStateFlow<Level?>("level", null)` y `getStateFlow("saved", emptyList<String>())`; los tres juntos con `combine(..., ::DraftUiState)`, y el resultado convertido en `StateFlow` con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftUiState())`.
2. **Los cambios**, escritos en el handle (`handle["text"] = ...`):
   - `onTextChange`: el texto cortado a `MAX_LENGTH`;
   - `onLevelChange`: el nivel;
   - `save`: solo si hay texto (sin contar los espacios) y nivel. Añade a `"saved"` la descripción sin espacios a los lados, y deja `"text"` en `""` y `"level"` en `null`. Lee el borrador del handle, no de `state`.
3. **`DraftScreen`, sin estado**: lo mismo que pintaba en el ejercicio 01, sacado de `state`, más un `Text` por cada descripción guardada, debajo de `"Guardadas: N"`. Cada cambio sale por su lambda.
4. **`DraftRoute`**: `val state by viewModel.state.collectAsStateWithLifecycle()` y `DraftScreen(state, viewModel::onTextChange, viewModel::onLevelChange, viewModel::save)`.
5. **Ejecuta** `./gradlew test` (9 de 9) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) En un test sin pantalla, `viewModel.onTextChange("riego")` y justo después `viewModel.state.value.text`. ¿Qué da? ¿Por qué? ¿Qué cambiaría con `SharingStarted.Eagerly`?
   - (b) Cambia `save` para que lea de `state.value` en vez del handle. ¿Qué test falla, y por qué solo ese? Restáuralo.
   - (c) Cambia el `ViewModel` para que guarde todo en un `MutableStateFlow<DraftUiState>` privado, sin el handle. ¿Qué test, de los que no miran el handle, falla? ¿Por qué pasa `theViewModelSurvivesARotation`? Restáuralo.
   - (d) ¿Qué cambia si recoges el estado con `collectAsState()` en vez de `collectAsStateWithLifecycle()`? ¿Lo nota algún test? ¿Lo notaría el móvil?
   - (e) ¿Por qué `DraftScreen` recibe el estado y unas lambdas, y no el `ViewModel`? ¿Qué tests no se podrían escribir?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'collectAsStateWithLifecycle'` | `import androidx.lifecycle.compose.collectAsStateWithLifecycle` (de `lifecycle-runtime-compose`) |
| Los de `DraftScreenTest` pasan y los de la actividad fallan con `could not find any node that satisfies: (Text + InputText + EditableText contains '5/80'...)` | `DraftRoute` no recoge `viewModel.state`: pinta un estado que no cambia |
| `onlyACompleteDraftIsSaved` falla y los de la pantalla pasan | `save` lee de `state.value`, que nadie recoge en ese test |
| `theDraftSurvivesProcessDeath` falla y `theViewModelSurvivesARotation` pasa | El borrador no está en el handle |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.

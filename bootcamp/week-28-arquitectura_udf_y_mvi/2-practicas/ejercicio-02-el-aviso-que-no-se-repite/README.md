# Ejercicio 02 — El aviso que no se repite

⏱️ 2 h · Teoría previa: 03, 04 y 05

## Objetivo

La lista de la salida del ejercicio 01, ahora con un ViewModel y un repositorio. Al terminar, el ViewModel tiene una sola entrada (`onIntent`), saca el estado de un repositorio que recibe por el constructor, guarda solo cuando la lista cambia, manda los avisos por un `Channel` y, si guardar falla, vuelve a lo guardado y avisa. Todo en la JVM, sin Android: el `ViewModel` de AndroidX también existe para la JVM.

## Cómo está montado

`starter/` es un proyecto de Kotlin para la JVM con `lifecycle-viewmodel` 2.11.0 y `kotlinx-coroutines` 1.11.0. Trae hechos el reducer del ejercicio 01 (resuelto), `ChecklistRepository` con `InMemoryChecklistRepository` (que cuenta los `save` y, con `failing = true`, lanza `IOException`) y `ChecklistMessage` (un efecto del reducer, o `SaveFailed`). `ChecklistViewModel` aplica el reducer y nada más, con comentarios `Paso N`.

Los tests (`src/test/kotlin/dev/bitacora/`, no los toques): `ViewModelTest` prueba el ViewModel; `AlternativesTest` mide lo que hacen un `SharedFlow` y un `StateFlow` con un efecto (teoría 03) y pasa desde el principio: es la medida, no la tarea. Al empezar, fallan 7 de 9.

## Pasos

1. **El estado sale del repositorio**: en `init`, `viewModelScope.launch { repository.items.collect { ... } }`, con `_state.update { it.copy(items = items) }`.
2. **Guardar lo que cambia**: en `onIntent`, guarda el estado de antes; si la lista de después es otra instancia (`!==`), `viewModelScope.launch { repository.save(after.items) }`. Escribir el borrador no guarda.
3. **Los avisos**: un `Channel<ChecklistMessage>(Channel.BUFFERED)` privado; `messages = channel.receiveAsFlow()`; el efecto del reducer, con `trySend(ChecklistMessage.Effect(it))` (teoría 03, apartado 2).
4. **Si guardar falla**: alrededor de `save`, `catch (_: IOException)`; la lista vuelve a la del repositorio (`repository.items.first()`) y sale `SaveFailed`, después del aviso del reducer.
5. Ejecuta `./gradlew test` (9 de 9) y `./gradlew build`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Cambia el `Channel` por un `MutableSharedFlow<ChecklistMessage>(extraBufferCapacity = 64)` y `tryEmit`. ¿Qué tests fallan, y qué tienen en común? ¿Qué momento de la vida de una actividad imitan? (Tres de ellos tardan un minuto cada uno: `runTest` los corta a los 60 s.) Restáuralo.
   - (b) `twoCollectorsSplitTheMessages`: ¿qué le llega a cada uno? Si una pantalla recogiese los avisos en dos sitios (la lista y una barra), ¿qué pasaría? ¿Dónde los recogerías?
   - (c) Cambia el cuerpo de `main()` en `ViewModelTest` por `Unit` (sin `Dispatchers.setMain`) y ejecuta `./gradlew test --tests '*theListComesFromTheRepository'`. ¿Lanza una excepción, o falla de otra forma? ¿Qué valor tiene la lista, y por qué? ¿Por qué en un móvil no pasa? Restáuralo.
   - (d) `aFailedSaveRollsBack` deja la lista como la tiene el repositorio. ¿Por qué no como estaba antes del intent? Piensa en dos intents seguidos con el disco lleno.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `theListComesFromTheRepository` falla con la lista vacía | El `collect` va en `init`, no en `onIntent` |
| `onlyChangesToTheListAreSaved` cuenta 3 | Guardas con cada intent: compara la lista de antes y la de después con `!==` |
| `aMessageWaitsForItsCollector` se queda colgado | `messages` sigue siendo `emptyFlow()`, o el `Channel` no tiene *buffer* (`Channel()` es de capacidad 0, y `trySend` falla sin nadie esperando) |
| `aFailedSaveRollsBack` recibe `SaveFailed` primero | Manda el efecto del reducer antes de lanzar el `save` |

## Entrega

Los 9 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.

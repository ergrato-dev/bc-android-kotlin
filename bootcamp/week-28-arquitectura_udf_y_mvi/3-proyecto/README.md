# Proyecto — Un estado, intents y un repositorio

⏱️ 4 h 30 min · Capa 27 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Abre la fase 5.

## Qué construyes

Hasta la semana 27, el ViewModel de la Bitácora tenía una función por acción, el estado salía de juntar cuatro flujos del `SavedStateHandle`, y las observaciones se perdían al cerrar la app. Esta semana, la sesión tiene un solo estado, todo lo que pasa entra por `onIntent`, las reglas están en un reducer puro, los avisos salen como efectos, y las observaciones son de un repositorio que las guarda en un archivo. La persona no ve nada distinto, salvo que al abrir la app sus observaciones siguen ahí.

## Dónde trabajas

En **tu repositorio**, rama `week-28`, a partir de lo que entregaste en la semana 27. El `starter/` es la Bitácora de la semana 27 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya:

- en el catálogo, `kotlinx-coroutines-core` y `kotlinx-coroutines-test` 1.11.0, y en el build, `api(libs.coroutines.core)` en `data` y `coroutines-test` en los tests;
- `Session.kt`: `BitacoraUiState` (con el campo nuevo `deleted`), `Locating`, `Deleted`, `BitacoraIntent`, `BitacoraEffect`, `Next`, y `reduce`, que devuelve el estado sin tocar;
- `BitacoraViewModel.kt`, con el constructor y `onIntent` vacío; el `ObservationDraftViewModel` de siempre sigue siendo el que usa la actividad;
- en `data`, `ObservationRepository` y `JsonFileObservationRepository`, que de momento solo guarda en memoria;
- `BitacoraApp.kt`, el contenedor, que todavía no está en el manifiesto.

Y tests nuevos o cambiados:

- `data/src/test/kotlin/dev/bitacora/data/RepositoryTest.kt`: el archivo;
- `app/src/test/kotlin/dev/bitacora/ReducerTest.kt` y `ViewModelTest.kt`, con `FakeRepository.kt`: el reducer y el ViewModel, en la JVM;
- `StabilityTest` y `FormTest`, que ahora usan `BitacoraViewModel`;
- `app/src/testField/kotlin/dev/bitacora/ArchitectureTest.kt`: cerrar y abrir la app, el archivo, y compartir una sola vez.

Con la Bitácora de la semana 27, fallan 2 de 6 en `:data:test`, 11 de 65 en `testFieldDebugUnitTest` y 9 de 62 en `testDemoDebugUnitTest`. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :data:test :app:testFieldDebugUnitTest`. Abre `ObservationDraftViewModel`: ¿cuántos sitios cambian el estado, y cuántos flujos lo forman? Apúntalo para el README (teoría 01).
2. **El archivo** (`data`): `JsonFileObservationRepository` lee al crearse (sin archivo, `initial`; uno que no se lee, a `.bad`) y en `save` cambia primero la memoria y después escribe en `io`, en un `.tmp` que se mueve encima con `ATOMIC_MOVE`, dentro de un `Mutex` (teoría 04, apartado 2).
3. **El reducer** (`Session.kt`): un caso por intent, sin `else`, con las reglas de las funciones de `ObservationDraftViewModel`: `Add` con el borrador sin espacios, la posición y `nextCode()`, y nada si está vacío o pasa de `MAX_DESCRIPTION`; `Delete` guarda `Deleted` y pide `ShowUndo`; `Share` solo pide `Share` (teoría 02).
4. **El ViewModel** (`BitacoraViewModel`): el estado de partida con lo que haya en el handle (`draft`, `position`, `locating`, `deleted` y `deletedAt`); en `init`, las observaciones del repositorio; `onIntent` reduce, guarda en el handle lo que no es la lista, llama a `save` si la lista es otra (`!==`) y manda el efecto por un `Channel(Channel.BUFFERED)` (teorías 03 y 04).
5. **La DI** (`AndroidManifest.xml`, `MainActivity`): `android:name=".BitacoraApp"`, y `viewModels { viewModelFactory { initializer { BitacoraViewModel(createSavedStateHandle(), (application as BitacoraApp).repository) } } }` (teoría 04, apartados 3 y 4).
6. **La actividad**: cada llamada a `notes` pasa a ser un `onIntent`; `BitacoraScreen` recibe el `SnackbarHostState` en vez de `onUndo`, y la actividad recoge los efectos en un `LaunchedEffect(Unit)`: `ShowUndo` enseña el aviso (y si se pulsa "Deshacer", `onIntent(Undo)`), `Share` abre el selector. Borra `ObservationDraftViewModel.kt` y `onUndo`.
7. **Los tests**: `./gradlew :core:test :data:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo? ¿Por qué? Y `./gradlew build`.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
   - en el reducer, `Replace` de A-2 a Baja cambia solo esa, en su sitio;
   - en el reducer, tras `LocationDenied`, `Located(null)` deja `Locating.IDLE` y sin posición;
   - en el reducer, borrar A-1, después A-3, y deshacer: vuelve A-3 y A-1 no;
   - en el ViewModel, `Share` no guarda nada (`saves == 0`) y manda el efecto con las tres;
   - en `data`, dos `save` seguidos: en el archivo, el segundo;
   - en demo, borrar A-1 y abrir la app otra vez sin Bundle: sigue borrada y hay "2 observaciones".
9. **Explica en tu README** lo que apuntaste en el paso 1 y cómo queda ahora; qué es estado y qué es efecto en la Bitácora, y por qué; qué sigue en el `SavedStateHandle` y qué en el repositorio; y qué pasa con el aviso de "borrada" si el proceso muere mientras se ve (teoría 03, apartado 5).
10. **Abre el pull request** `week-28` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, un solo `UiState`, una sola entrada, un reducer puro sin `else` y sin Android, los efectos por un `Channel` recogidos en un solo sitio, el repositorio detrás de una interfaz y recibido por el constructor, el archivo escrito sin perder datos, y tests con valores esperados escritos a mano.

## Pistas

- `sharingHappensOnce` pasa desde el principio: la Bitácora de la semana 27 ya compartía una vez. Es una guarda para que el efecto no acabe en el estado.
- Si `theListSurvivesClosingTheApp` falla con `ClassCastException`, falta `android:name` en el manifiesto.
- Si los tests de borrar de la semana 25 no ven el aviso, el `LaunchedEffect` no recoge `notes.effect`, o `BitacoraScreen` no usa el `SnackbarHostState` que le pasas.
- Si `typingKeepsTheSameObservations` falla, el reducer crea una lista nueva al cambiar el borrador: `copy(draft = ...)` y nada más.
- `FormTest.theViewModelRefusesTooLong` pasa desde el principio: con `onIntent` vacío tampoco se guarda nada. Pasa a probar algo de verdad cuando escribes el paso 4.
- `./gradlew :app:testFieldDebugUnitTest --tests '*ReducerTest'` para ir test a test.

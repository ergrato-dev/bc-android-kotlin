# Proyecto — La pantalla principal en Compose

⏱️ 4 h 30 min · Capa 20 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Abre la fase 4.

## Qué construyes

La pantalla principal de la Bitácora deja de ser un árbol de vistas. La pinta `BitacoraScreen`, un composable sin estado que recibe lo que tiene que enseñar y avisa de lo que hace el usuario. El estado vive en un solo sitio, el `ObservationDraftViewModel`, que lo guarda en su `SavedStateHandle` y lo publica como un `StateFlow`. La actividad se queda con lo que es de Android: pedir permisos, ubicar, abrir la pantalla de la observación y compartir. Por fuera, la Bitácora hace lo mismo que en la semana 20, en los dos sabores y en los dos idiomas.

La pantalla de la observación (`ObservationActivity`) sigue con vistas. Pasará a Compose cuando la navegación sustituya los `Intent` (semana 24).

## Dónde trabajas

En **tu repositorio**, rama `week-21`, a partir de lo que entregaste en la semana 20. El `starter/` es la Bitácora de la semana 20 resuelta, con comentarios `Paso N` donde va cada cambio. Ya trae:

- en el catálogo, las bibliotecas y el plugin de Compose;
- en `app/build.gradle.kts`, el BOM de Compose y lo que necesitan los tests (`ui-test-junit4` y `ui-test-manifest`);
- los tests de la semana 20 que miran la pantalla principal, reescritos para Compose: buscan los nodos por su texto con `createEmptyComposeRule` y pulsan como el usuario, en vez de leer las vistas de la actividad.

Con la pantalla de vistas, esos tests fallan con `IllegalStateException: No compose hierarchies found in the app`: 16 de 23 en `testFieldDebugUnitTest` y 2 de 5 en `testDemoDebugUnitTest`. Los de `ObservationActivity` y los que solo miran el título siguen en verde. En `src/test/kotlin/BuildTest.kt`, el test de ejemplo `composeIsInTheApp` empieza en rojo: busca el runtime de Compose en el APK. Copia todo eso a tu repositorio.

## Pasos

1. **Compose en el build**:
   - en el raíz, `alias(libs.plugins.kotlin.compose) apply false`;
   - en `app`, el plugin `alias(libs.plugins.kotlin.compose)` y las bibliotecas `activity-compose` (en lugar de `activity`), `compose-material3`, `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`. Van en la app y no en el plugin de convención: son de esta pantalla.
2. **El estado de la pantalla** (`ObservationDraftViewModel.kt`):
   - `enum class Locating { IDLE, LOCATING, DENIED }`: en qué está la ubicación mientras el borrador no tiene una;
   - `data class BitacoraUiState(draft, position, locating, observations)`, con valores por defecto;
   - el `SavedStateHandle` se llama ahora `handle`, y `val state: StateFlow<BitacoraUiState>` sale de `combine` sobre `getStateFlow` de `"draft"`, `"position"` (el `DoubleArray`), `"locating"` y `"observations"` (el JSON), con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`. Sin observaciones guardadas, las de `Samples`, también en el valor inicial.
3. **Las acciones** del `ViewModel`: `onDraftChange(draft)`, `locating()`, `denied()`, `located(position)` (que vuelve a `IDLE`), `add(level)` (que además deja la ubicación en `null` y en `IDLE`) y `replace(observation)`. Todas leen del handle, nunca de `state` (teoría 05).
4. **La pantalla** (`BitacoraScreen.kt`, nuevo): `BitacoraScreen(title, state, onDraftChange, onLocate, onAdd, onOpen, onShare)`, sin estado. En una `Column`, de arriba abajo:
   - el título;
   - un `TextField` con la etiqueta "Descripción";
   - el texto de la ubicación: las coordenadas si hay posición; si no, "Ubicando…", "Sin permiso de ubicación" o "Sin ubicación", según `locating`;
   - los botones "Ubicar", uno por nivel y, al final, "Compartir";
   - entre los niveles y "Compartir", el resumen y un `TextButton` por observación, que la abre.
   
   El resumen y la línea de cada observación se calculan con dos funciones de `Resources` (`summary` y `line`), fuera de Compose, porque la actividad también las usa para compartir.
5. **La actividad**: `setContent` con `BitacoraScreen` y el estado recogido con `collectAsStateWithLifecycle`. Fuera las vistas y las propiedades públicas que solo usaban los tests (`summary`, `description`, `list`, `place`, `add`, `share`...). El `ViewModel`, privado.
6. **Los tests de la semana 20**: `./gradlew :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest`, hasta el verde. Si uno no encuentra un nodo, compara su texto con el que pinta tu pantalla.
7. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:testDemoDebugUnitTest :app:testFieldDebugUnitTest :app:lintFieldDebug` y `git diff --stat gradle/verification-metadata.xml`. ¿Qué entra? ¿Por qué `ui-test-junit4` no aparece como nueva? Pasa los artefactos nuevos por [OSV](https://osv.dev) antes de fijarlos.
8. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**:
   - en `app/src/test`, `BitacoraScreen` con un `BitacoraUiState` hecho a mano: lo que pinta (título, borrador, resumen, observaciones);
   - `BitacoraScreen` solo avisa: escribir, ubicar, un nivel, abrir una observación y compartir llegan a sus lambdas, y la pantalla no cambia;
   - el texto de la ubicación con `LOCATING`, con `DENIED`, y con una posición, que gana;
   - en `app/src/testField`, el `ViewModel` con un `SavedStateHandle` hecho a mano (un borrador y una observación en JSON): `add` devuelve la `A-2` y vacía el borrador;
   - un borrador en blanco no toca el handle;
   - en `BuildTest`, la actividad vacía de `ui-test-manifest` está en el APK de debug y no en el de release.
9. **Explica en tu README** dónde vive cada parte del estado de la pantalla (y por qué ahí y no en `remember`), por qué las acciones del `ViewModel` leen del handle, y qué test fallaría si `BitacoraScreen` guardase su propia copia del borrador.
10. **Abre el pull request** `week-21` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, `BitacoraScreen` sin estado, el estado en el `SavedStateHandle`, las acciones leyendo del handle, ninguna vista en `MainActivity`, y tests con valores calculados a mano.

## Pistas

- Dentro de un composable, los recursos son `LocalResources.current`; en la actividad, `resources`. Así `summary` y `line` sirven a los dos.
- `onShare` puede pasar a la actividad las observaciones que la pantalla está enseñando: `onShare = { share(state.observations) }`.
- Los tests de la semana 20 ya no llaman a métodos de la actividad: escriben en "Descripción" y pulsan "Alta". Si renombras un texto de los recursos, fallan.
- `LanguagesTest` busca los botones por su papel (`Role.Button`), en orden. Un `Button` de Material lo tiene.
- Un test solo de la pantalla vale para los dos sabores: va en `src/test`. Uno del `ViewModel` con las observaciones de `field` va en `src/testField` (semana 20, teoría 03).
- `./gradlew :app:testFieldDebugUnitTest --tests '*MainActivityTest'` para ir test a test.

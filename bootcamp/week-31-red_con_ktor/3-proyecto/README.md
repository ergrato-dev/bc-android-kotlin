# Proyecto — Las observaciones del equipo

⏱️ 4 h 30 min · Capa 30 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Hasta la semana 30, la Bitácora solo sabía lo que se apuntaba en el móvil. Esta semana, «Actualizar» pide al servidor del equipo sus observaciones (`GET observations`) y las guarda en Room: las del servidor pisan a las de aquí con el mismo código, y las que solo están aquí se quedan. La pantalla las ve llegar por el `Flow` de siempre. Mientras se espera, el botón se desactiva; al terminar, un aviso dice cuántas han llegado, o qué ha fallado con palabras de la app: sin conexión, el servidor tarda demasiado, el servidor ha fallado, o ha mandado algo que no se entiende. Cada flavor habla con su servidor.

El servidor contesta esto:

```json
{"observations": [
  {"code": "A-1", "level": "high", "description": "roya en el lote 2", "lat": 4.6, "lng": -74.08},
  {"code": "A-2", "level": "low", "description": "pulgón"}
]}
```

## Dónde trabajas

En **tu repositorio**, rama `week-31`, a partir de lo que entregaste en la semana 30. El `starter/` es la Bitácora de la semana 30 resuelta, con comentarios `Paso N` donde va cada cambio. Trae ya:

- en el catálogo, Ktor 3.6.1 (`ktor-client-core`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`, `ktor-client-okhttp` y `ktor-client-mock`); en `data`, el cliente sin motor, `ContentNegotiation` y el JSON, y `MockEngine` en los tests; en `app`, el motor de OkHttp; y `verification-metadata.xml` con los 61 componentes nuevos;
- en `data/Network.kt`, `RemoteObservation` (sin los nombres del servidor), `ObservationsResponse`, `NetworkError`, `RefreshResult`, `CONNECT_TIMEOUT` y `REQUEST_TIMEOUT`; `bitacoraClient` sin plugins, `ObservationApi` que devuelve una lista vacía y `toNetworkError` que devuelve `null`;
- `refresh()` en `ObservationRepository`; en `RoomObservationRepository`, que ahora recibe una `ObservationApi`, un `refresh()` que no pide nada y devuelve `Done(0)`;
- en `AppModule`, el `@Provides @Singleton` del `HttpClient` con OkHttp y una URL escrita a mano, y `observations(database, client)`;
- en `app/build.gradle.kts`, `buildFeatures.buildConfig = true` y `API_URL` en los dos flavors (la de la demo, mal);
- en `Session.kt`, `refreshing`, los *intents* `Refresh` y `Refreshed` y el efecto `ShowRefresh`, que el reducer aún ignora; en `MainActivity`, `refreshMessage`, que devuelve `""`, y `onRefresh` hacia la pantalla; los textos, en español y en inglés;
- en el `FakeRepository` de los tests, `refresh()` con `answer` y `refreshes`.

Y tests nuevos:

- `data/src/test/kotlin/dev/bitacora/data/NetworkTest.kt`: el cliente y `refresh()` con `MockEngine`, en tiempo virtual;
- `app/src/test/kotlin/dev/bitacora/RefreshTest.kt`: el reducer y el ViewModel con el `FakeRepository`;
- `app/src/testField/kotlin/dev/bitacora/RefreshScreenTest.kt`: «Actualizar» y su aviso, en Robolectric con `FakeModule`;
- `ServerTest.kt` en `testField` y en `testDemo`: la URL de cada flavor;
- `AccessibilityTest` y `LanguagesTest` cuentan ya el botón nuevo, y `RoomTest`, `DatabaseTest` y `ArchitectureTest` crean el repositorio con su `ObservationApi`.

Con la Bitácora de la semana 30 y estos cambios, fallan 11 de 27 en `:data:test` (todo `NetworkTest`), 9 de 91 en `testFieldDebugUnitTest` y 5 de 79 en `testDemoDebugUnitTest`. Copia esos archivos a tu repositorio.

## Pasos

1. **Lee y ejecuta**: `./gradlew :data:test`. ¿Por qué `NetworkTest` no necesita red? Mira en `server(...)` qué dispatcher recibe el `MockEngine`, y piensa qué pasaría con `aSlowServerTimesOut` sin él (teoría 05, apartado 3).
2. **Las clases del servidor** (`RemoteObservation`): `@SerialName("lat")` y `@SerialName("lng")`, con `= null`: una observación puede no tener posición (teoría 02, apartado 2).
3. **El cliente** (`bitacoraClient`): `expectSuccess = true`; `ContentNegotiation` con `json(Json { ignoreUnknownKeys = true })`; `HttpTimeout` con `connectTimeoutMillis = CONNECT_TIMEOUT` y `requestTimeoutMillis = REQUEST_TIMEOUT`; y `defaultRequest { url(baseUrl) }` (teoría 02, apartado 1, y 03, apartado 2).
4. **La petición** (`ObservationApi.observations`): `client.get("observations")`, sin `/` delante, leído como `ObservationsResponse` y traducido con una función privada `RemoteObservation.toObservation(): Observation?`. El nivel llega en minúsculas; uno que la app no conoce da `null`, y `mapNotNull` lo salta (teoría 04, apartado 1).
5. **Los errores y `refresh()`**: `toNetworkError()` con un `when`, los tiempos agotados antes que `IOException` (teoría 03, apartado 3). En `refresh()`, un `try` alrededor de `api.observations()`; en el `catch (e: Exception)`, `Failed(e.toNetworkError() ?: throw e)`; si llega, `dao.upsert(...)` y `Done(received.size)` (teoría 04, apartado 2).
6. **El grafo y los flavors**: en `AppModule`, `BuildConfig.API_URL` en lugar de la URL escrita; en `app/build.gradle.kts`, la de la demo es `https://demo.bitacora.example/v1/`. Declara `INTERNET` en el manifiesto de la app: ¿cambia el APK? (`aapt2 dump badging`, o `onlyTheLocationAndMapPermissionsShip` en el `BuildTest` de la raíz.) ¿Por qué el `HttpClient` lleva `@Singleton`? (teoría 04, apartados 4 y 5).
7. **El reducer y el ViewModel**: `Refresh` pone `refreshing` (si ya lo estaba, `Next(state)` tal cual); `Refreshed` lo quita y devuelve el efecto `ShowRefresh`. En `onIntent`, cuando `refreshing` pasa de `false` a `true`, `viewModelScope.launch { onIntent(BitacoraIntent.Refreshed(repository.refresh())) }` (teoría 04, apartado 3).
8. **La pantalla**: en `BitacoraScreen`, el botón «Actualizar» (`R.string.refresh`) junto a «Compartir» y «Mapa», con `enabled = !state.refreshing`, en un `FlowRow` (con 320 dp, tres no caben en una fila). En `MainActivity`, `refreshMessage` con un `when` por resultado, y `ShowRefresh` que lo enseña con `snackbar.showSnackbar(...)` dentro de un `launch`.
9. **Los tests y la verificación**: `./gradlew :data:test :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :app:lintFieldDebug` hasta el verde. Después, `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:lintFieldDebug :app:testFieldDebugUnitTest :app:testDemoDebugUnitTest :data:test` y `git diff --stat gradle/verification-metadata.xml`: ¿entra algo? Y `./gradlew build`. Mira el tamaño de `app-field-release-unsigned.apk` antes y después de esta semana.
10. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano** y el porqué en un comentario:
    - en `data`, una observación del servidor sin `lat` ni `lng` llega sin posición (no con `0.0, 0.0`);
    - en `data`, una respuesta con `text/plain` es `BAD_RESPONSE`, no un fallo sin nombre;
    - en `data`, `refresh()` sin red no toca la base de datos: lo que había sigue igual;
    - en `data`, con un servidor que contesta 503: `refresh()` hace una sola llamada (la Bitácora no reintenta en el cliente; lo hará WorkManager en la semana 32);
    - en el ViewModel, `Refreshed` con un error deja `refreshing` a `false` y no cambia la lista;
    - en Robolectric, mientras el `FakeRepository` no contesta (`answer.value = null`), «Actualizar» está desactivado (`assertIsNotEnabled()`).
11. **Explica en tu README** por qué `RemoteObservation`, `ObservationEntity` y `Observation` son tres clases; por qué `refresh()` escribe en Room en vez de devolver la lista a la pantalla; qué error de tu dominio necesita un mensaje distinto y por qué; qué pasaría si `refresh()` se tragase la cancelación; y qué cambio hecho aquí y no subido se perdería con «Actualizar» (lo que arregla la semana 32).
12. **Abre el pull request** `week-31` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintFieldDebug` en verde, la red en `data` sin motor y sin Android, el cliente `@Singleton` con OkHttp y la URL del flavor, tiempos máximos, errores con nombre que no se tragan la cancelación, lo recibido en Room, y tests con valores esperados escritos a mano.

## Pistas

- `NoTransformationFoundException` en todos los tests de `NetworkTest`: falta `ContentNegotiation` (paso 3).
- `Field 'latitude' is required`: la propiedad se llama `latitude` y el JSON, `lat`. Y luego `Field 'lat' is required`: falta el `= null`.
- `theRequestGoesToTheBaseUrl` da `https://api.bitacora.example/observations`: la ruta empieza por `/`.
- `notFoundIsAServerError` y `aServerErrorIsAnError` dan `BAD_RESPONSE`: falta `expectSuccess`, y se intenta leer como lista el cuerpo del error.
- `aSlowServerTimesOut` da `OFFLINE`: en `toNetworkError`, `IOException` va antes que los tiempos agotados.
- `aSlowServerTimesOut` da `Done(2)` las dos veces: falta `HttpTimeout`.
- `cancellingIsNotAnError` falla: `refresh()` convierte la cancelación en un error (`?: NetworkError.OFFLINE`, o `runCatching`).
- `oneRequestAtATime` da `refreshes` 0: el ViewModel no lanza `refresh()`, o lo lanza antes de que el reducer ponga `refreshing`.
- `theDemoTalksToTheDemoServer` falla: la URL de la demo en `app/build.gradle.kts` (paso 6).
- `everythingClickableHasALabelAndRoomForAFinger` cuenta 11: falta el botón (paso 8).
- `./gradlew :data:test --tests '*NetworkTest'` para ir test a test.

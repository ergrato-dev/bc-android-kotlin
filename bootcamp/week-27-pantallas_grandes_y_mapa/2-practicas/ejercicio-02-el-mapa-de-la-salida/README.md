# Ejercicio 02 — El mapa de la salida

⏱️ 2 h · Teoría previa: 04 y 05

## Objetivo

Una pantalla "Mapa" con tres observaciones de una salida al campo, dos con ubicación y una sin ella. Al terminar, la clave de Maps va en el manifiesto sin estar en el repositorio, cada observación sabe dónde está, hay un rectángulo que las contiene a todas, y la pantalla tiene un `GoogleMap` con un marcador por observación, la cámara encima de todas y un texto que dice cuántas salen.

## Cómo está montado

`starter/` trae hechos `Observation` (con `latitude` y `longitude` opcionales), las tres de la salida, `MainActivity`, el tema y los textos (`R.string.map` y `R.string.on_map`), y en el build `maps-compose` 9.0.1 y `play-services-maps` 20.1.0. `MapScreen.kt` tiene `latLng()` y `bounds()`, que siempre dicen `null`, y una pantalla con solo el encabezado, con comentarios `Paso N`.

Los tests (`app/src/test/kotlin/dev/bitacora/`, no los toques): `BoundsTest` prueba `latLng()` y `bounds()`; `MapScreenTest`, la pantalla, el `MapView`, el manifiesto y los permisos. Al empezar, fallan 5 de 8.

No hace falta una clave para nada de esto: en Robolectric el mapa no carga (teoría 05). Si tienes una y un móvil con Play Services, ponla en `~/.gradle/gradle.properties` (`mapsApiKey=...`), nunca en el proyecto.

## Pasos

1. **La clave**: en `app/build.gradle.kts`, `manifestPlaceholders["mapsApiKey"] = providers.gradleProperty("mapsApiKey").getOrElse("")`; en el manifiesto, dentro de `<application>`, la `meta-data` `com.google.android.geo.API_KEY` con `android:value="${mapsApiKey}"` (teoría 04, apartado 3).
2. **Dónde está** (`latLng()`, `bounds()`): un `LatLng` si tiene las dos coordenadas; el rectángulo con `LatLngBounds.builder()`, un `include` por punto y `build()`, o `null` si no hay ninguno (el builder vacío lanza una excepción).
3. **Cuántas salen**: debajo del encabezado, `stringResource(R.string.on_map, placed.size, observations.size)`, con `placed` las que tienen `latLng()`.
4. **El mapa**: un `GoogleMap` con `Modifier.fillMaxWidth().weight(1f)` y `rememberCameraPositionState()`. Dentro, por cada una de `placed`, `key(observation.code) { Marker(state = rememberUpdatedMarkerState(...), title = observation.code, snippet = observation.description) }`. En `onMapLoaded`, `bounds()?.let { camera.move(CameraUpdateFactory.newLatLngBounds(it, 64)) }` (teoría 04, apartados 4 y 5).
5. Ejecuta `./gradlew test` (8 de 8) y `./gradlew build`, sin la propiedad `mapsApiKey`.
6. **`NOTAS.md`** (en `starter/`):
   - (a) Quita la `meta-data` del manifiesto. ¿Qué test falla? Según la documentación, ¿qué pasaría en un móvil? Restáurala.
   - (b) Ejecuta `./gradlew assembleDebug -PmapsApiKey=prueba` y `$ANDROID_HOME/build-tools/<versión>/aapt2 dump xmltree --file AndroidManifest.xml app/build/outputs/apk/debug/app-debug.apk`. ¿Dónde aparece `prueba`? Si la clave va dentro del APK, ¿qué la protege?
   - (c) `theMapBringsTheNetworkPermissions` pasa desde el principio, sin que toques nada. ¿Por qué? ¿Quién pone esos permisos, y para qué sirve el test?
   - (d) ¿Qué hace tu mapa que ningún test comprueba? ¿Cómo lo comprobarías?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `theKeyIsInTheManifest` falla con el placeholder puesto | La `meta-data` va dentro de `<application>`, no de `<activity>` |
| `Attribute meta-data#...@value ... requires a placeholder substitution but no value for <mapsApiKey> is provided` | Falta `manifestPlaceholders["mapsApiKey"]` en `defaultConfig` |
| `IllegalStateException: no included points` | `bounds()` construye el rectángulo sin ningún punto: devuelve `null` antes |
| `NullPointerException: CameraUpdateFactory is not initialized` | La cámara se mueve antes de que haya mapa: dentro de `onMapLoaded` |
| `theMapIsAMapViewWithoutPlayServices` no encuentra el `MapView` | El `GoogleMap` no está en la pantalla: va dentro de la `Column`, después del texto |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 6.

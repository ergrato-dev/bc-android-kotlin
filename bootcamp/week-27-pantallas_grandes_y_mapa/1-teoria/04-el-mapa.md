# El mapa con Maps Compose

> Hasta ahora, la ubicación de una observación eran dos números. Un mapa los pone en su sitio: se ve de un vistazo qué parcela tiene problemas y cuáles están juntas. Es la primera dependencia de la Bitácora que no es de AndroidX ni de Kotlin, y trae con ella una clave de API, permisos de red y 70 artefactos nuevos en la verificación.

## 🎯 Objetivos

- Elegir la librería del mapa y saber qué trae
- Tener la clave de API fuera del repositorio
- Pintar un `GoogleMap` con un marcador por observación y la cámara sobre todas
- Revisar lo que entra en el build con una dependencia nueva

## 1. Qué librería

| Opción | Estado (2026-11-04) | Qué pide |
| --- | --- | --- |
| **Maps Compose** (`com.google.maps.android:maps-compose` 9.0.1) sobre `play-services-maps` 20.1.0 | Mantenida por Google, Apache 2.0 | Google Play Services en el móvil y una clave de API |
| MapLibre (`org.maplibre.gl:android-sdk`) | Mantenida, BSD | Una fuente de teselas; lleva código nativo |
| osmdroid 6.1.20 | Repositorio archivado desde 2024-11 | Nada, pero ya no recibe arreglos |

La Bitácora usa Maps Compose: es lo que se encuentra en la mayoría de los proyectos Android, y su API es Compose de verdad. MapLibre es la opción abierta; osmdroid queda fuera por archivado: una dependencia sin mantenimiento no recibe parches de seguridad.

> 🔬 Revisado el 2026-11-04: sin avisos en [OSV](https://osv.dev) para `maps-compose` 9.0.1, `play-services-maps` 20.1.0, `play-services-base` 18.5.0, `play-services-basement` 18.9.0, `play-services-tasks` 18.2.0 ni `android-maps-utils-core` 6.0.0. `maps-compose` 9.0.1 importa el BOM de Compose 2026.09.00: el mismo de la Bitácora.

## 2. Las dependencias

```kotlin
implementation(libs.maps.compose)
implementation(libs.play.services.maps)
implementation(libs.fragment)
```

> 🔬 Medido: `maps-compose` 9.0.1 trae `play-services-maps` 20.0.0, también para compilar. La dependencia directa no hace falta para que compile: sube la versión a la 20.1.0, la última estable, y deja escrito en el build de quién son `LatLng` y `CameraUpdateFactory`. Y `play-services-maps` declara `androidx.fragment` 1.0.0: en la Bitácora, `lintFieldDebug` para con `InvalidFragmentVersionForActivityResult` ("Upgrade Fragment version to at least 1.3.0") en el `registerForActivityResult` de la actividad, aunque Gradle resuelva la 1.5.4. Con `fragment` 1.9.1 directa, lint pasa. En el ejercicio 02, que no usa `registerForActivityResult`, lint pasa sin ella.

Y entran solos dos permisos en el manifiesto final:

> 🔬 Medido en el APK de debug: `play-services-maps` añade `android.permission.INTERNET` y `android.permission.ACCESS_NETWORK_STATE` (el comentario de su manifiesto: "Include required permissions for Google Maps API to run"). El `BuildTest` de la semana 19, que listaba los permisos, los tiene que esperar. Son permisos normales: no se piden en tiempo de ejecución.

En `verification-metadata.xml` entran 70 componentes nuevos: Maps, Play Services, adaptive, fragment y lo que traen (entre ellos, `kotlinx-coroutines-android` 1.11.0 y `kotlin-stdlib-jdk7/jdk8` 2.4.20). Se regenera con el comando de siempre y se revisa con `git diff --stat` antes de aceptarlo (semana 12).

## 3. La clave de API

Maps necesita una clave de Google Cloud (con la Maps SDK for Android activada), en el manifiesto:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${mapsApiKey}" />
```

`${mapsApiKey}` es un *placeholder*: AGP lo sustituye al fusionar el manifiesto. Sale de una propiedad de Gradle, igual que la firma de la semana 20:

```kotlin
defaultConfig {
    manifestPlaceholders["mapsApiKey"] = providers.gradleProperty("mapsApiKey").getOrElse("")
}
```

La clave va en `~/.gradle/gradle.properties` (`mapsApiKey=...`) o en `ORG_GRADLE_PROJECT_mapsApiKey` en CI, nunca en el repositorio. Sin ella, la app compila, los tests pasan, y el mapa sale vacío.

La clave no es un secreto de verdad: va dentro del APK, y cualquiera la saca con `aapt2 dump`. Lo que la protege es la **restricción** en Google Cloud: solo para la Maps SDK for Android, y solo para tu `applicationId` con el SHA-1 de tu certificado de firma. Sin restricción, alguien que la saque gasta tu cuota.

Según la documentación de Maps, si falta la entrada `com.google.android.geo.API_KEY`, el mapa lanza una excepción al iniciarse en el móvil; con la entrada vacía, el mapa sale gris y el log dice que la clave no es válida. Por eso la entrada va siempre, aunque esté vacía, y un test lo comprueba (teoría 05).

## 4. `GoogleMap` y los marcadores

```kotlin
@Composable
fun MapScreen(observations: List<Observation>, onOpen: (Observation) -> Unit, modifier: Modifier = Modifier) {
    val placed = observations.filter { it.position != null }
    val camera = rememberCameraPositionState()
    Column(modifier.padding(16.dp)) {
        Text(stringResource(R.string.map), Modifier.semantics { heading() })
        Text(stringResource(R.string.on_map, placed.size, observations.size))
        GoogleMap(
            Modifier.fillMaxWidth().weight(1f),
            cameraPositionState = camera,
            onMapLoaded = { ... },
        ) {
            for (observation in placed) {
                key(observation.code) {
                    Marker(
                        state = rememberUpdatedMarkerState(observation.position!!.toLatLng()),
                        title = observation.code,
                        snippet = resources.line(observation),
                        onInfoWindowClick = { onOpen(observation) },
                    )
                }
            }
        }
    }
}
```

- `GoogleMap` es un `MapView` de los de siempre metido en Compose (`AndroidView`); el contenido (la lambda del final) es otra composición, la del mapa, donde viven los marcadores.
- `key(observation.code)` hace lo mismo que la clave de una `LazyColumn`: al borrar una, cada marcador sigue siendo de la suya.
- `rememberUpdatedMarkerState` mueve el marcador si cambia la posición.
- Las que no tienen posición no están en el mapa, y el mapa no lo dice: por eso el texto "1 de 3 en el mapa".

## 5. La cámara

Al abrir, la cámara tiene que enseñar todas las observaciones. `LatLngBounds` es el rectángulo que las contiene, y `CameraUpdateFactory.newLatLngBounds(bounds, padding)` la mueve encima.

> 🔬 Medido en la JVM: `CameraUpdateFactory.newLatLngBounds(...)` sin un mapa cargado lanza `NullPointerException: CameraUpdateFactory is not initialized`. Y `LatLngBounds.builder().build()` sin ningún punto, `IllegalStateException: no included points`.

Así que la cámara se mueve en `onMapLoaded`, cuando ya hay mapa, y solo si hay algo que enseñar:

```kotlin
onMapLoaded = {
    observations.area()?.let { (southWest, northEast) ->
        camera.move(
            if (southWest == northEast) CameraUpdateFactory.newLatLngZoom(southWest.toLatLng(), 15f)
            else CameraUpdateFactory.newLatLngBounds(LatLngBounds(southWest.toLatLng(), northEast.toLatLng()), 64),
        )
    }
},
```

Con un solo punto, el rectángulo mide cero y la cámara se acercaría al máximo: un zoom fijo se ve mejor. El área se calcula en `core`, sin Play Services (`area()`, en el proyecto), y se prueba como cualquier función.

## ✅ Comprueba

- [ ] ¿Por qué osmdroid no, aunque no pida clave?
- [ ] ¿Para qué se declara `play-services-maps` si ya la trae `maps-compose`? ¿Por qué la Bitácora necesita `fragment` y el ejercicio 02 no?
- [ ] ¿Qué permisos entran con el mapa, y quién los pone?
- [ ] La clave va dentro del APK. ¿Qué la protege, entonces? ¿Por qué no va en el repositorio?
- [ ] ¿Por qué la cámara se mueve en `onMapLoaded` y no al componer?

# Probar lo que no se pinta

> Los tests de la Bitácora corren en la JVM, con Robolectric, y Robolectric no tiene Google Play Services. El mapa es la primera pieza de la app que en los tests no hace su trabajo. Esta teoría es lo que pasa de verdad, qué se puede probar alrededor, y qué queda para un móvil.

## 🎯 Objetivos

- Saber qué hace `GoogleMap` en Robolectric
- Sacar del mapa lo que se puede probar sin él
- Probar el manifiesto y los permisos que trae una dependencia
- Tener una lista de lo que se comprueba a mano en un móvil

## 1. El mapa en Robolectric

> 🔬 Medido en una sonda: `GoogleMap` no falla en Robolectric. Crea un `com.google.android.gms.maps.MapView` del tamaño que le toca, y dentro, un `TextView` con el aviso de Play Services, traducido: "No es posible ejecutar la aplicación Bitácora demo sin los Servicios de Google Play, que no son compatibles con tu dispositivo." (en inglés, "... won't run without Google Play services, which are not supported by your device."). El contenido no se compone nunca: con un `SideEffect` contador dentro, 0 tras 5 segundos de reloj; `onMapLoaded` no llega.

Es decir: los marcadores, la cámara y `onInfoWindowClick` no existen en los tests. Ni en el árbol de semántica (los marcadores no son nodos de Compose), ni en las vistas.

No hay forma limpia de darle Play Services a Robolectric: la librería del mapa carga su código de la app de Play Services del móvil, no del APK. Lo que se hace es dejar en el mapa lo mínimo, y probar el resto.

## 2. Lo que se saca del mapa

Todo lo que se puede calcular sin mapa, se calcula fuera:

| Qué | Dónde | Cómo se prueba |
| --- | --- | --- |
| Qué observaciones salen | `filter { it.position != null }` | el texto "1 de 3 en el mapa" |
| El rectángulo de la cámara | `area()` en `core` | tests de Kotlin puro, en `:core:test` |
| De `Position` a `LatLng` y `LatLngBounds` | funciones en `app` | tests de Robolectric: son clases de datos |

> 🔬 Medido: `LatLng` y `LatLngBounds.builder().include(...).build()` funcionan en la JVM (con Robolectric): `center`, `contains` y `southwest`/`northeast` dan lo esperado. Lo que no funciona sin mapa es `CameraUpdateFactory` (teoría 04).

## 3. Lo de alrededor

Lo que sí está en los tests:

```kotlin
// La entrada de la clave está en el manifiesto, aunque el build no tenga mapsApiKey.
@Test
fun theManifestHasTheMapsKey() {
    val info = activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
    assertTrue(info.metaData.containsKey("com.google.android.geo.API_KEY"))
}

// "Mapa" abre un destino con un MapView de verdad, que en Robolectric dice que falta Play Services.
@Test
fun theMapIsAMapView() {
    rule.onNodeWithText("Mapa").performClick()
    val views = activity.window.decorView.all()
    assertTrue(views.any { it.javaClass.name == "com.google.android.gms.maps.MapView" })
    assertTrue(views.any { (it as? TextView)?.text?.contains("Google Play") == true })
}
```

El segundo compara el nombre de la clase como texto: así compila aunque el test no tenga `play-services-maps` en su classpath (en el starter del proyecto, todavía no está).

Los permisos que trae la dependencia, en el APK (el `BuildTest` del raíz, con `aapt2`) o en Robolectric:

```kotlin
val info = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
assertEquals(listOf("android.permission.ACCESS_NETWORK_STATE", "android.permission.INTERNET", ...), info.requestedPermissions!!.sorted())
```

Un test así falla si una dependencia nueva mete un permiso: la próxima vez que lo haga, te enteras.

## 4. Lo que queda para el móvil

Sin `/dev/kvm` no hay emulador en este entorno, y lo que sigue no se ha visto en un móvil. Con un móvil con Play Services y una clave restringida:

- el mapa carga con la clave, y sale gris sin ella;
- la cámara enseña todas las observaciones al abrir, y con una sola, un zoom de calle;
- tocar un marcador enseña su globo, y tocar el globo abre la observación;
- con el modo oscuro, el mapa también (`mapColorScheme`, por defecto sigue al sistema);
- en un plegable o una tableta, el mapa a la derecha de la sesión.

Apúntalo en el README como prueba manual, con lo que viste.

## ✅ Comprueba

- [ ] ¿Qué hay dentro del `MapView` en Robolectric? ¿Se componen los marcadores?
- [ ] ¿Qué partes del mapa de la Bitácora se prueban en `core`, cuáles en Robolectric y cuáles solo en un móvil?
- [ ] ¿Por qué `theMapIsAMapView` compara el nombre de la clase como texto?
- [ ] ¿Para qué sirve un test que lista los permisos del APK?

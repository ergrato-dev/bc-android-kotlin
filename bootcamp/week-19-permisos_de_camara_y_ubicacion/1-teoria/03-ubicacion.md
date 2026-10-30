# Ubicación

> En el campo, dónde se hizo una observación importa tanto como lo que dice. Android da la ubicación con dos niveles de detalle, y desde Android 12 el usuario elige cuál concede. La Bitácora tiene que funcionar con los dos y sin ninguno.

## 🎯 Objetivos

- Pedir la ubicación aproximada y la precisa, y trabajar con la que conceda el usuario
- Obtener la ubicación actual con `LocationManagerCompat`, sin Google Play Services
- Simular ubicaciones en Robolectric

## 1. Aproximada y precisa

| Permiso | Precisión | Proveedor |
| --- | --- | --- |
| `ACCESS_COARSE_LOCATION` | Una zona de unos 3 km², a propósito | Red (antenas y wifi) |
| `ACCESS_FINE_LOCATION` | La mejor que dé el móvil, metros | GPS |

Desde Android 12 (API 31), el diálogo deja elegir entre "precisa" y "aproximada", así que se piden **los dos a la vez**. Si se pide solo la precisa, Android 12 ignora la petición (guía oficial, webgrafía).

> 🔬 Medido en el ejercicio 01: lint no avisa si solo pides la precisa. Hay que saberlo.

El usuario puede conceder la aproximada y no la precisa. La app usa lo que tenga:

```kotlin
val precise = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
val provider = if (precise) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
```

La ubicación en segundo plano (`ACCESS_BACKGROUND_LOCATION`) es otro permiso, que se pide aparte, después de los otros dos, y que Google Play revisa a mano. La Bitácora ubica solo cuando el usuario pulsa "Ubicar", con la app abierta: no lo necesita.

## 2. La ubicación actual

```kotlin
LocationManagerCompat.getCurrentLocation(
    getSystemService(LocationManager::class.java),
    provider,
    CancellationSignal(),
    ContextCompat.getMainExecutor(this),
) { location -> show(location) }
```

`getCurrentLocation` pide **una** ubicación reciente y llama al callback una vez: con la ubicación, o con `null` si no la consigue (proveedor apagado, sin señal). Para seguir al usuario mientras se mueve existe `requestLocationUpdates`, que la Bitácora no necesita.

`LocationManagerCompat` es la versión de `androidx.core`: usa `getCurrentLocation` en Android 11 o superior y lo imita en versiones anteriores. Con `CancellationSignal` de `android.os`, no con `null`:

```text
e: .../MainActivity.kt:40:31 Overload resolution ambiguity between candidates:
```

> 🔬 Medido en la sonda: hay dos versiones de la función, una con la `CancellationSignal` de `android.os` y otra con la antigua de `androidx.core.os`, y `null` encaja en las dos.

La otra opción habitual es `FusedLocationProviderClient`, de Google Play Services: mezcla GPS, red y sensores, y gasta menos batería. Pero añade una dependencia, solo funciona en móviles con Google Play, y `LocationManager` basta para una ubicación por observación.

## 3. En Robolectric

`ShadowLocationManager` hace de GPS:

```kotlin
val locations = app.getSystemService(LocationManager::class.java)
shadowOf(locations).simulateLocation(Location(LocationManager.GPS_PROVIDER).apply {
    latitude = -34.6037
    longitude = -58.3816
})
shadowOf(Looper.getMainLooper()).idle()
```

> 🔬 Medido en la sonda y en el ejercicio 01:
> - Con el permiso concedido, `getCurrentLocation` espera hasta `simulateLocation` y entrega esa ubicación.
> - Sin permiso, no llama nunca al callback ni lanza nada. En un móvil, lanzaría `SecurityException`: por eso lint obliga a comprobar.
> - El proveedor de red empieza **apagado** (`isProviderEnabled(NETWORK_PROVIDER)` es `false`) y el GPS encendido. Con la red apagada, `getCurrentLocation` responde enseguida con `null`. Los tests de ubicación aproximada lo encienden con `setProviderEnabled(NETWORK_PROVIDER, true)`.
> - Con el proveedor apagado después de una ubicación, devuelve la última conocida. En un móvil, `null`.

## ✅ Comprueba

- [ ] ¿Por qué se piden los dos permisos de ubicación aunque la app prefiera la precisa?
- [ ] El usuario concede solo la aproximada. ¿Qué proveedor usa la Bitácora, y qué precisión puede esperar?
- [ ] ¿Por qué un test de ubicación que no simula nada no falla en Robolectric, y qué pasaría en un móvil?

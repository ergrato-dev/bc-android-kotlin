# Ejercicio 01 — Pedir permiso

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

Un botón "Locate" que recorre el flujo completo de un permiso: ubica sin preguntar si ya tiene permiso, explica antes de pedirlo cuando Android lo aconseja, pide los dos permisos de ubicación a la vez, usa la red si solo le dan la aproximada, y manda a los ajustes cuando el usuario ha dicho que no para siempre. Al final, lint comprueba que nadie usa la ubicación sin mirar antes.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Robolectric. `MainActivity` tiene un texto (`status`), el botón "Locate", que llama a `onLocate()`, y un botón de ajustes oculto (`settings`). Ya están hechos `show()`, que pinta la ubicación, y `openSettings()`. Faltan el lanzador, `onLocate()` y `locate()`. Los textos están en `res/values/strings.xml`.

Los tests (`app/src/test/kotlin/dev/bitacora/PermissionsTest.kt`, no los toques) hacen de usuario: conceden permisos con `grantPermissions`, responden a la petición con `answer()` y mueven el GPS con `simulateLocation` (teoría 03). Los 7 empiezan en rojo.

## Pasos

1. **El manifiesto**: `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`.
2. **El lanzador** (`RequestMultiplePermissions`), como propiedad. En el callback:
   - con alguno concedido, `locate()`;
   - con los dos denegados y `shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)` en `true`, `R.string.denied`;
   - si es `false`, `R.string.denied_forever` y `settings` visible.
3. **`onLocate()`**:
   - con la aproximada concedida, `locate()`;
   - si Android aconseja explicar y aún no lo has hecho (`explained`), `R.string.rationale`, sin pedir nada;
   - si no, pide los dos permisos a la vez.
4. **`locate()`**:
   - Empieza con `ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)`, y sale si no está concedido.
   - Con la precisa, `GPS_PROVIDER`; solo con la aproximada, `NETWORK_PROVIDER`.
   - `R.string.locating`, y `LocationManagerCompat.getCurrentLocation(...)` con una `CancellationSignal()` y `ContextCompat.getMainExecutor(this)`. El resultado va a `show(location, precise)`.
5. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew :app:lintDebug`.
6. **Lee** `aFirstDenialLetsYouTryAgain` y `deniedForeverOffersTheSettings`: la única diferencia es `userNeedsAReason()`. ¿Qué simula, y cuándo lo diría Android en un móvil?
7. **`NOTAS.md`** (en `starter/`):
   - (a) En `locate()`, cambia la comprobación por `if (!granted(ACCESS_COARSE_LOCATION)) return`. ¿Qué dice lint? ¿Por qué, si hace lo mismo? Restáuralo.
   - (b) Pide solo `ACCESS_FINE_LOCATION`. ¿Dice algo lint? ¿Qué haría un móvil con Android 12? Restáuralo.
   - (c) Quita la variable `explained` (y su comprobación). ¿Qué test falla? ¿Qué vería el usuario? Restáurala.
   - (d) Quita `ACCESS_FINE_LOCATION` del manifiesto. ¿Siguen los tests en verde? ¿Y lint? ¿Qué pasaría en un móvil al pedirlo? Restáuralo.
   - (e) En `approximateOnlyUsesTheNetwork`, comenta la línea que enciende el proveedor de red. ¿Qué texto queda? ¿Por qué?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Overload resolution ambiguity between candidates` en `getCurrentLocation` | Pasa `CancellationSignal()` de `android.os`, no `null` (teoría 03) |
| `[MissingPermission]` en lint | La comprobación tiene que estar en el mismo método que la llamada, con `ContextCompat.checkSelfPermission` |
| `La app no pidió ningún permiso` | `onLocate()` no llama a `launch`, o el lanzador no se ha registrado como propiedad |
| `expected:<[-34.6037, -58.3816]> but was:<[Locating…]>` | `locate()` no pasa el resultado a `show()`, o usa otro proveedor |
| `whenTheUserNeedsAReasonItExplainsFirst` pide permiso a la primera | Falta comprobar `shouldShowRequestPermissionRationale` antes de `launch` |
| `LifecycleOwners must call register before they are STARTED` | El lanzador va como propiedad, no dentro de `onLocate()` (semana 18) |

## Entrega

Los 7 tests en verde, `lintDebug` en verde y `NOTAS.md` con las respuestas del paso 7.

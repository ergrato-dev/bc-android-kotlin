# Glosario — Semana 19

| Término | Definición |
| --- | --- |
| **aapt2 dump badging** | Muestra lo que Google Play lee del APK: permisos y funciones de hardware, también las implícitas |
| **ACCESS_BACKGROUND_LOCATION** | Permiso para la ubicación con la app cerrada; se pide aparte y Google Play lo revisa |
| **ACCESS_COARSE_LOCATION** | Ubicación aproximada, una zona de unos 3 km², por la red |
| **ACCESS_FINE_LOCATION** | Ubicación precisa, por el GPS; se pide junto con la aproximada |
| **ACTION_APPLICATION_DETAILS_SETTINGS** | Abre la pantalla de ajustes de una app, donde el usuario puede conceder un permiso |
| **ACTION_IMAGE_CAPTURE** | La acción de la app de la cámara; la usa `TakePicture` |
| **CameraX** | Biblioteca de Jetpack para usar la cámara dentro de la app; necesita `CAMERA` |
| **checkSelfPermission** | Dice si un permiso está concedido ahora mismo |
| **content://** | `Uri` de un proveedor de contenido; da acceso a un dato sin dar su ruta |
| **Denegado para siempre** | Tras dos "no" (Android 11 o superior), la app ya no puede mostrar el diálogo |
| **file_paths.xml** | Lista de las carpetas que un `FileProvider` puede prestar |
| **FileProvider** | Proveedor de `androidx.core` que convierte archivos de la app en `Uri` `content://` con permiso temporal |
| **FileUriExposedException** | Fallo al sacar una `Uri` `file://` de la app en un Intent, desde Android 7 |
| **Función implícita** | Función de hardware que Google Play deduce de un permiso, como la cámara de `CAMERA` |
| **getCurrentLocation** | Pide una ubicación reciente y llama una vez al callback, con ella o con `null` |
| **grantPermissions** | Método de `ShadowApplication` que concede permisos en un test de Robolectric |
| **LocationManagerCompat** | La versión de `androidx.core` de `LocationManager`, que funciona igual en todas las versiones |
| **Manifest merger blame** | Informe de la fusión del manifiesto con el origen de cada línea |
| **MissingPermission** | Error de lint: una llamada que necesita un permiso sin comprobarlo antes en el mismo método |
| **Permiso de firma** | Se concede solo a las apps firmadas con la misma clave (`protectionLevel` `signature`) |
| **Permiso normal** | Se concede al instalar, sin preguntar, como `INTERNET` |
| **Permiso peligroso** | Lo concede el usuario en ejecución, con un diálogo, como la ubicación o la cámara |
| **PickVisualMedia** | Contrato del selector de fotos del sistema; no necesita permisos |
| **Proveedor de ubicación** | La fuente de la ubicación: `gps`, `network` o `passive` |
| **RequestMultiplePermissions** | Contrato que pide varios permisos a la vez y devuelve un mapa permiso → concedido |
| **RequestPermission** | Contrato que pide un permiso y devuelve un `Boolean` |
| **shouldShowRequestPermissionRationale** | Si conviene explicar el permiso antes de volver a pedirlo; `true` solo tras un "no" |
| **simulateLocation** | Método de `ShadowLocationManager` que hace de GPS en un test |
| **TakePicture** | Contrato que abre la app de la cámara y guarda la foto en la `Uri` que le das |
| **tools:node="remove"** | Marcador de la fusión del manifiesto que quita un elemento que trae una biblioteca |
| **uses-feature** | Declara una función de hardware y si es obligatoria (`android:required`) |
| **uses-permission** | Declara un permiso en el manifiesto; sin él, no se puede conceder |

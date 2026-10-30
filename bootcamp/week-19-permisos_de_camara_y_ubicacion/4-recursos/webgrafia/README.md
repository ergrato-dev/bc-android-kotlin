# Webgrafía — Semana 19

| Recurso | Para qué |
| --- | --- |
| [Permisos en Android](https://developer.android.com/guide/topics/permissions/overview) | Los niveles de protección y el modelo de permisos en ejecución |
| [Pedir permisos en ejecución](https://developer.android.com/training/permissions/requesting) | El flujo, `shouldShowRequestPermissionRationale` y lo que cambia en Android 11 |
| [Pedir la ubicación](https://developer.android.com/develop/sensors-and-location/location/permissions) | Aproximada y precisa, lo que cambia en Android 12, segundo plano |
| [`LocationManagerCompat`](https://developer.android.com/reference/androidx/core/location/LocationManagerCompat) | `getCurrentLocation` y el resto de la API compatible |
| [Hacer fotos](https://developer.android.com/media/camera/camera-intents) | La app de la cámara con un Intent, y cuándo hace falta `CAMERA` |
| [`MediaStore.ACTION_IMAGE_CAPTURE`](https://developer.android.com/reference/android/provider/MediaStore#ACTION_IMAGE_CAPTURE) | `EXTRA_OUTPUT` y la `SecurityException` si declaras `CAMERA` sin concederlo |
| [`FileProvider`](https://developer.android.com/reference/androidx/core/content/FileProvider) | Autoridad, `file_paths.xml`, `getUriForFile` y por qué no se exporta |
| [Selector de fotos](https://developer.android.com/training/data-storage/shared/photo-picker) | `PickVisualMedia`, sin permisos |
| [Fusión de manifiestos](https://developer.android.com/build/manage-manifests) | Prioridades, marcadores (`tools:node`) y el informe de la fusión |
| [`uses-feature` y Google Play](https://developer.android.com/guide/topics/manifest/uses-feature-element) | Las funciones implícitas de cada permiso y cómo filtra Google Play |
| [`ShadowLocationManager`](https://robolectric.org/javadoc/latest/org/robolectric/shadows/ShadowLocationManager.html) | `simulateLocation`, `setProviderEnabled` |

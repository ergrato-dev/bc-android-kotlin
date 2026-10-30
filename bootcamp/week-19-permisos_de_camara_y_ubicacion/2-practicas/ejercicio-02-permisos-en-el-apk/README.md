# Ejercicio 02 — Permisos en el APK

⏱️ 2 h · Teoría previa: 01, 04 y 05

## Objetivo

Decidir qué permisos y qué funciones de hardware lleva el APK: los de la app, ninguno que traiga una biblioteca sin que lo sepas, y la cámara y la ubicación como funciones no obligatorias, para que Google Play no esconda la app en una tablet sin cámara. Además, preparar el `FileProvider` que prestará las fotos a la app de la cámara, y que lint no encuentre ninguna llamada a la ubicación sin comprobar el permiso.

## Cómo está montado

Como en la semana 17: `starter/sandbox/` es un proyecto Android, y los tests de `starter/src/test/kotlin/BuildTest.kt` (no los toques) lo copian a una carpeta temporal, lo construyen con TestKit y abren el APK con `apkanalyzer` y `aapt2 dump badging` (`Apk.badging()`). Necesitas `ANDROID_HOME` y `build-tools` 36.0.0.

El proyecto tiene dos módulos:

- `:app`, la app, que usa el SDK y lee la última ubicación conocida.
- `:sdk`, que hace de SDK de analítica de un tercero. No lo toques: en un proyecto real no es tu código. Su manifiesto declara `INTERNET` y `READ_CONTACTS`.

Los tests empiezan en rojo, salvo `lintStopsALocationCallWithoutACheck`, que empieza en verde y tiene que seguir así.

## Pasos

Todo se hace dentro de `sandbox/app/`.

1. **Los permisos de la app** (`src/main/AndroidManifest.xml`): `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` y `CAMERA`. La app tendrá una pantalla con la cámara dentro (CameraX, en otra fase).
2. **Fuera los contactos**: el SDK no tiene por qué leerlos. Quita su `READ_CONTACTS` con `tools:node="remove"` (y el espacio de nombres `xmlns:tools` en `<manifest>`).
3. **Funciones no obligatorias**: `<uses-feature>` con `android:required="false"` para `android.hardware.camera` y `android.hardware.location`. Usa esos nombres exactos (teoría 05).
4. **El `FileProvider`**:
   - En el manifiesto, `<provider>` con `androidx.core.content.FileProvider`, `android:authorities="${applicationId}.files"`, `exported="false"`, `grantUriPermissions="true"` y el `<meta-data>` `android.support.FILE_PROVIDER_PATHS`, que apunta a `@xml/file_paths`.
   - `src/main/res/xml/file_paths.xml`, con un solo `<files-path name="photos" path="photos/" />`.
5. **La comprobación**: `./gradlew -p sandbox :app:lintDebug` y lee el error. En `lastPlace()`, devuelve `null` si `ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)` no es `PERMISSION_GRANTED`.
6. **Ejecuta** `./gradlew test` (7 de 7).
7. **`NOTAS.md`** (en `starter/`):
   - (a) Quita el `tools:node="remove"` y construye. ¿Qué permiso vuelve? Busca la línea en `app/build/intermediates/manifest_merge_blame_file/debug/processDebugMainManifest/manifest-merger-blame-debug-report.txt`. ¿Quién la trajo? Restáuralo.
   - (b) Cambia `android.hardware.camera` por `android.hardware.camera.any` en el `uses-feature`. ¿Qué dice `aapt2 dump badging`? ¿Qué tablets dejarían de ver la app en Google Play? Restáuralo.
   - (c) Quita los dos `uses-feature`. ¿Qué funciones aparecen como `uses-implied-feature`, y por qué motivo según `aapt2`?
   - (d) ¿Por qué `INTERNET` no necesita diálogo y `CAMERA` sí? ¿Qué es `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, quién lo trae y quién más puede tenerlo? (`apkanalyzer manifest print` y busca `<permission`.)
   - (e) La app declara `CAMERA`. Si un día abre la app de la cámara con `TakePicture` sin haberlo pedido, ¿qué pasa? ¿Y si no lo declarase? (teoría 04)

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `The prefix "tools" for attribute "tools:node" associated with an element type "uses-permission" is not bound` (con `--stacktrace`) | Falta `xmlns:tools="http://schemas.android.com/tools"` en `<manifest>` |
| `onlyThePermissionsTheAppUsesShip` sigue con `READ_CONTACTS` | El `tools:node="remove"` tiene que estar en un `<uses-permission>` con el mismo `android:name` |
| `theCameraIsOptional` encuentra `uses-feature: name='android.hardware.camera'` | El `uses-feature` no se llama exactamente `android.hardware.camera` |
| `AAPT: error: resource xml/file_paths (aka dev.bitacora.agro:xml/file_paths) not found.` | El archivo no está en `src/main/res/xml/`, o tiene otro nombre |
| `[MissingPermission]` | La comprobación tiene que estar en `lastPlace()`, antes de `getLastKnownLocation` |
| `No such file` al ejecutar `aapt2` | Falta `build-tools` 36.0.0: `sdkmanager "build-tools;36.0.0"` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 7.

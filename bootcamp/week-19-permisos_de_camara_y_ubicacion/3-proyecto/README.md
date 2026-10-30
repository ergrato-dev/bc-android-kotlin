# Proyecto — La Bitácora sabe dónde y lo ve

⏱️ 4 h 30 min · Capa 18 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Cada observación puede llevar dónde se hizo y una foto. En la pantalla principal, un botón "Ubicar" pide la ubicación (y el permiso, si hace falta), y la siguiente observación que añades la lleva. En la pantalla de la observación, un botón "Foto" abre la app de la cámara, que guarda la foto en un archivo de la Bitácora. Al elegir el nivel, la observación vuelve con su foto. Sin permiso de ubicación, la Bitácora funciona igual. La foto no necesita ningún permiso, y llega aunque Android mate la app mientras el usuario está en la cámara.

## Dónde trabajas

En **tu repositorio**, rama `week-19`, a partir de lo que entregaste en la semana 18. El `starter/` es la Bitácora de la semana 18 resuelta, con sus tests y con comentarios `Paso N` donde va cada cambio. `src/test/kotlin/Apk.kt` trae ya `badging()`, que ejecuta `aapt2 dump badging` sobre el APK.

En `src/test/kotlin/BuildTest.kt` hay un test de ejemplo que empieza en rojo: comprueba los permisos del APK. Los tests de Robolectric de la semana 18 empiezan en verde. Copia todo eso a tu repositorio.

## Pasos

1. **`core`**: `data class Position(val latitude: Double, val longitude: Double)`, y en `Observation` dos campos opcionales con `null` por defecto: `position` y `photo` (el nombre del archivo, no el archivo). `core` sigue sin saber nada de Android.
2. **`data`**: los campos nuevos en el DTO, opcionales (`latitude`, `longitude` y `photo`, con `null` por defecto). El JSON de una observación sin ubicación ni foto tiene que ser el mismo que antes: lo comprueba `roundTrip`, en `DataTest`.
3. **El manifiesto**: `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`, y `android.hardware.location` como función no obligatoria. `CAMERA`, no: la foto la hace la app de la cámara (teoría 04).
4. **El borrador** (`ObservationDraftViewModel`): `position`, la ubicación del borrador, en el `SavedStateHandle`. Un `Bundle` no guarda una `Position`; un `DoubleArray`, sí. `add()` se la pone a la observación nueva y la borra.
5. **Ubicar** (`MainActivity`):
   - Un texto (`place`) con la ubicación del borrador, o "Sin ubicación", y un botón "Ubicar".
   - Con la aproximada concedida, busca la ubicación. Si no, pide las dos. Si las deniega, "Sin permiso de ubicación".
   - Con la precisa, el GPS; solo con la aproximada, la red. `LocationManagerCompat.getCurrentLocation`, como en el ejercicio 01. La comprobación del permiso va en el mismo método que la llamada.
6. **El `FileProvider`**: en el manifiesto, con autoridad `${applicationId}.files`, y `res/xml/file_paths.xml` con la carpeta `photos/` de `files`.
7. **La foto** (`ObservationActivity`):
   - Muestra la ubicación de la observación, si la tiene, y si tiene foto o no.
   - Un botón "Foto" lanza `TakePicture` con la `Uri` de `files/photos/<código>.jpg`.
   - Si la cámara la guarda (`true`), la observación pasa a tener `photo = "<código>.jpg"`. Si no, nada cambia.
8. **La muerte del proceso**: la cámara suele hacer que Android mate la Bitácora. Guarda el nombre de la foto en `onSaveInstanceState` y recupéralo en `onCreate`.
9. **Los textos**: los nuevos, en los dos idiomas. Actualiza los tests de la semana 17 que cuentan los botones de la pantalla.
10. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease :app:testDebugUnitTest :app:lintDebug` y `git diff --stat gradle/verification-metadata.xml`. ¿Cambia algo? ¿Por qué?
11. **Escribe al menos 6 tests más**, con el valor esperado escrito **a mano**:
    - con permiso, "Ubicar" no pide nada, muestra la ubicación y la siguiente observación la lleva,
    - sin permiso, pide los dos, y al concederlos ubica,
    - denegado, la observación se guarda sin ubicación,
    - "Foto" lanza `IMAGE_CAPTURE` con `content://dev.bitacora.agro.files/photos/A-1.jpg` y los permisos de esa `Uri`,
    - una foto guardada vuelve con la observación; una cancelada no cambia nada,
    - la foto llega a la pantalla recreada tras la muerte del proceso,
    - en `data`, una observación con ubicación y foto ida y vuelta, con el JSON escrito a mano,
    - en `BuildTest`, la ubicación no es obligatoria en `badging()` y el `FileProvider` no está exportado.
12. **Explica en tu README** por qué la Bitácora no declara `CAMERA`, qué ve el usuario con la ubicación aproximada, y qué hace la app si el usuario dice que no para siempre.
13. **Abre el pull request** `week-19` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, `lintDebug` en verde sin `@SuppressLint`, solo los permisos de ubicación, `FileProvider` sin exportar, `core` y `data` sin Android, la foto tras la muerte del proceso y tests con valores calculados a mano.

## Pistas

- `FileProvider` recuerda las carpetas de la primera vez en una variable estática (`sCache`), y Robolectric da a cada test una carpeta de datos nueva. Si el segundo test de fotos falla con `Failed to find configured root`, vacía `sCache` en un `@Before` (teoría 04).
- Para responder a la petición de permisos en un test, `dispatchResult` con `EXTRA_PERMISSIONS` (un `Array<String>`, escrito con `arrayOf`) y `EXTRA_PERMISSION_GRANT_RESULTS`, como en el ejercicio 01.
- La ubicación y el permiso ya concedido llegan por el hilo principal: `shadowOf(Looper.getMainLooper()).idle()` después de `onLocate()` y de `simulateLocation`.
- Para crear la pantalla de la observación con un estado guardado, `Robolectric.buildActivity(..., intent).setup(saved)`. `setup(null)` no vale: sin estado, `setup()`.
- `shadowOf(screen).resultIntent` es lo que la pantalla puso con `setResult`.

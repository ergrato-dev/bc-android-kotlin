# Ejercicio 02 — Firmar sin secretos

⏱️ 2 h · Teoría previa: 05

## Objetivo

Que el release de la app salga firmado con una clave de subida, sin que la clave ni sus contraseñas estén en el proyecto. Las contraseñas llegan de fuera, por la línea de comandos o por variables de entorno, como en un servidor de integración continua. Sin ellas, el build no falla: el release sale sin firmar. Todo se comprueba en el APK, con `apksigner`.

## Cómo está montado

Como en la semana 19: `starter/sandbox/` es un proyecto Android, y los tests de `starter/src/test/kotlin/BuildTest.kt` (no los toques) lo copian a una carpeta temporal y lo construyen con TestKit. Necesitas `ANDROID_HOME` y `build-tools` 36.0.0.

La app ya tiene los tipos de build y los flavors del ejercicio 01. Su firma de release está mal hecha a propósito: las contraseñas están escritas en `sandbox/app/build.gradle.kts`, y el almacén, `upload.jks`, tendría que estar dentro del proyecto.

Los tests no usan tu clave. `Keystore.kt` crea una de usar y tirar con `keytool`, fuera del proyecto, y se la pasa al build con las propiedades `uploadStoreFile`, `uploadStorePassword`, `uploadKeyAlias` y `uploadKeyPassword`. Empiezan en rojo, salvo `theDebugBuildKeepsTheDebugKey`, que empieza en verde y tiene que seguir así.

## Pasos

Todo se hace en `sandbox/app/build.gradle.kts`.

1. **Fuera las contraseñas**: la configuración `release` lee las cuatro propiedades con `providers.gradleProperty(...)`. `storeFile` es un archivo: `.orNull?.let(::file)`.
2. **Sin clave, sin firma**: `release` usa esa configuración solo si `uploadStoreFile` está presente (`isPresent`).
3. **Ejecuta** `./gradlew test` (7 de 7).
4. **Tu clave**: crea una con `keytool -genkeypair` (teoría 05) **fuera** del proyecto. Construye el release a mano con tus `-P` y compruébalo:
   - `./gradlew -p sandbox :app:assembleFieldRelease -PuploadStoreFile=... -PuploadStorePassword=... -PuploadKeyAlias=... -PuploadKeyPassword=...`
   - `apksigner verify --print-certs -v` sobre el APK.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Compara el SHA-256 del certificado que da `apksigner` con el de `keytool -list -v -keystore ...`. ¿Coinciden? ¿Qué te dice eso?
   - (b) Ejecuta `./gradlew -p sandbox :app:signingReport` sin y con tus `-P`. ¿Qué cambia en `fieldRelease`? ¿Y en `fieldDebug`?
   - (c) Cambia `minSdk` a 23 y vuelve a construir el release firmado. ¿Qué esquemas de firma tiene ahora? ¿Por qué? Restáuralo.
   - (d) Firma a mano `app-field-release-unsigned.apk` con `apksigner sign` (teoría 05). ¿Qué esquemas tiene? ¿Qué archivo más aparece?
   - (e) Lee `withTestKitDir` en `Sandbox.kt`. Si pusieras las cuatro propiedades `upload*` en `~/.gradle/gradle.properties`, ¿qué test fallaría en tu máquina, y por qué? ¿Dónde guardarías tú las contraseñas de verdad?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Keystore file '.../app/upload.jks' not found for signing config 'release'.` | Sigue el `file("upload.jks")` del starter: la ruta tiene que salir de `uploadStoreFile` |
| `Keystore file not set for signing config release` | Asignas la configuración aunque no haya almacén: falta el `if (...isPresent)` |
| `keystore password was incorrect` | `uploadStorePassword` no es la del almacén |
| `Given final block not properly padded` | `uploadKeyPassword` no es la de la clave |
| `Contraseña escrita en build.gradle.kts` | Queda un `storePassword = "..."` o `keyPassword = "..."` |
| `withoutTheKeyTheReleaseIsUnsigned` falla solo en tu máquina | Tienes las propiedades `upload*` en `~/.gradle/gradle.properties`: el build de los tests las lee (paso 5e) |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5. Ni el almacén ni sus contraseñas en ningún archivo del ejercicio.

# Proyecto — La Bitácora segura

⏱️ 5 h · Capa 37 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

El servidor de campo empieza a pedir un token: cada móvil se vincula una vez y manda el token en cada petición. Esta semana la Bitácora:

- **Guarda el token cifrado** con una clave del Keystore (Tink), y lo manda como `Authorization: Bearer`.
- **Se vincula con un enlace**, `bitacora://pair?token=…`, que valida lo que llega y pide confirmación antes de guardarlo.
- **Fija su red**: nada en claro, el servidor de campo con dos pines y una caducidad, y las CA de la persona solo en la debug.
- **Deja el token fuera de las copias de seguridad**, y las observaciones dentro.
- **Revisa lo que exporta** y **escanea sus dependencias**.

```
app                                              core:network
  Token.kt: TokenStore · pairingToken · keystoreAead   bitacoraClient(engine, url, token)
  AppModule: client(Lazy<TokenStore>)                    defaultRequest { bearerAuth(token) }
  SecurityModule: TokenStore(files/token.bin)
  MainActivity: bitacora://pair → snackbar → save
res/xml
  network_security_config.xml · data_extraction_rules.xml · backup_rules.xml
```

## Dónde trabajas

En **tu repositorio**, rama `week-38`, a partir de lo que entregaste en la semana 37. El `starter/` es la Bitácora de la semana 37 resuelta. Trae ya:

- en `gradle/libs.versions.toml`, Tink 1.24.0 (`libs.tink.android`);
- en `gradle/verification-metadata.xml`, lo que trae: `tink-android` y 5 componentes más (`gson` 2.13.2, `error_prone_annotations` 2.41.0, `annotation-jvm` 1.8.2 y dos POM padre), revisados en OSV, sin avisos;
- `server/api.pem` (el certificado del servidor de campo) y `server/backup.pem` (la clave pública de reserva).

Y un test nuevo:

- `src/test/kotlin/SecurityTest.kt`, en la raíz, con TestKit: que no hay tráfico en claro, que `api.bitacora.example` está fijado con las dos claves de `server/`, que las CA de la persona solo valen en la debug, que solo `MainActivity` está abierta a cualquiera, que el token no va a las copias y que `bitacora://pair` abre la app.

Con la Bitácora de la semana 37 y estos cambios, fallan 5 de los 6 de `SecurityTest`. Los demás siguen en verde. Copia esos archivos a tu repositorio.

**Para el paso 9 hace falta un dispositivo** (o KVM para el emulador). Sin él, todo lo demás se hace igual.

## Pasos

1. **Lee y ejecuta**: `./gradlew :test --tests 'SecurityTest'`. ¿Por qué ya pasa `onlyTheLauncherIsOpenToEveryone`?
2. **Mira por fuera** (teoría 04, apartado 1): `apkanalyzer manifest print` de la release y de la debug de field. ¿Qué componentes están exportados, de quién son y qué permiso piden? ¿Qué hay en la debug que no está en la release?
3. **La red** (teorías 02 y 03): `res/xml/network_security_config.xml` y `android:networkSecurityConfig`. Nada en claro en `base-config`. Calcula con `openssl` los pines de `server/api.pem` y `server/backup.pem`, y fija con ellos **solo** `api.bitacora.example`, con caducidad. En `debug-overrides`, las CA de la persona con `overridePins`. Pasan `cleartextIsForbidden`, `theServerIsPinned` y `userCertificatesOnlyInDebug`.
4. **El token** (teoría 01, apartado 5): en `app`, `implementation(libs.tink.android)`, y un `TokenStore(file, aead)` que cifra con AEAD y el contexto `"bitacora-token"`, y devuelve `null` si no puede abrir. La `Aead`, de `AndroidKeystore` (créala la primera vez). Un `SecurityModule` de Hilt, aparte de `AppModule` (los tests cambian `AppModule` por el suyo), que lo da con `files/token.bin`.
5. **El token en cada petición**: `bitacoraClient` recibe `token: () -> String?` y lo pone con `bearerAuth` en `defaultRequest`. `AppModule.client` recibe `Lazy<TokenStore>`: el Keystore se abre en la primera petición, fuera del hilo principal. `./gradlew test`: ¿qué tests de semanas anteriores dejan de compilar, y por qué? Arréglalos sin cambiar lo que comprueban.
6. **Vincular** (teoría 04, apartado 2): `<data android:host="pair" />` en el filtro de los enlaces. Una función `pairingToken(intent)` que solo acepta `bitacora://pair?token=` con un token de `[A-Za-z0-9_-]{20,200}`. En `MainActivity`, si llega uno, un snackbar «¿Vincular este móvil con el servidor?» con la acción *Vincular*; solo al tocarla, `save`, en `Dispatchers.IO`. Los textos, en español y en inglés. Pasa `aLinkPairsThePhone`.
7. **Las copias** (teoría 05, apartado 3): `data_extraction_rules.xml` y `backup_rules.xml`, que excluyen `token.bin` y nada más, conectados en el manifiesto. Pasa `theTokenStaysOutOfBackups`.
8. **Las dependencias** (teoría 06): la verificación, como en la semana 37 (`--write-verification-metadata sha256` con las mismas tareas, y `git diff --stat`). ¿Entra algo que no traiga el starter? Escanea `gradle/verification-metadata.xml` en OSV (teoría 06, apartado 2). ¿Cuántos componentes tienen avisos? Para cada uno, ¿llega al APK o solo al build? (`./gradlew :app:dependencies --configuration fieldReleaseRuntimeClasspath`.) Después, `./gradlew build`.
9. **Con un dispositivo**:
   - Añade a `DeviceTest` un test con el `TokenStore` de verdad: guarda, lee, que el archivo no contiene el token y que la clave del Keystore no se puede sacar (`getKey(...).encoded`). `./gradlew :app:connectedFieldDebugAndroidTest`.
   - `adb shell am start -a android.intent.action.VIEW -d "bitacora://pair?token=tk_4f9a2c7e1b0d5a8e3c6f" dev.bitacora.agro.debug`: ¿sale el snackbar? Tócalo, y mira `files/token.bin` con `adb shell run-as dev.bitacora.agro.debug xxd files/token.bin`. ¿Cuántos bytes tiene?
   - Con un token raro (`token=corto`, `token=a%22%3E%3Cscript%3E…`): ¿qué pasa?
10. **Escribe al menos 5 tests más**, con lo esperado escrito **a mano** y el porqué en un comentario. Por ejemplo:
    - `TokenStore` con una `Aead` de Tink en memoria (Robolectric no tiene Keystore): guarda y lee; el archivo no contiene el token y mide el token más 33; con otra clave, `null`; con un bit cambiado, `null`;
    - `pairingToken`: el enlace bueno, otro host, otro esquema, un token corto, uno con caracteres raros;
    - en `:core:network`, con `MockEngine`: sin token no hay `Authorization`, y el token guardado después de crear el cliente viaja en la siguiente petición;
    - un test de Hilt (`@UninstallModules(SecurityModule::class)` y `@BindValue` con tu `TokenStore` de pruebas): sin tocar *Vincular* no se guarda nada; tocándolo, sí.
11. **Explica en tu README** qué protege el Keystore y qué no; por qué el token no va en las copias y las observaciones sí; qué fijaste, con qué claves y qué pasa el día que el servidor cambie la suya; qué exporta tu app y por qué; y qué encontró el escaneo de dependencias. Si no tienes dispositivo, dilo.
12. **Abre el pull request** `week-38` → `main` con la salida de `./gradlew test`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md):

- el token cifrado con una clave del Keystore, fuera del hilo principal, y `null` si no se puede abrir;
- el enlace de vinculación validado y confirmado;
- la red sin tráfico en claro, el servidor de campo fijado con dos pines y caducidad, las CA de la persona solo en debug;
- el token fuera de las copias, y nada más;
- `SecurityTest` y los tests de la semana 37 en verde (los que dejaron de compilar, arreglados sin debilitarlos), sin warnings de Kotlin, `lintFieldDebug` en verde y tests con los valores esperados escritos a mano;
- las dependencias escaneadas, con lo que se encontró explicado.

## Pistas

- `No value passed for parameter 'tokens'` en `DatabaseTest` y `ArchitectureTest`: llaman a `AppModule.client()` a mano. No piden nada al servidor: un `Lazy<TokenStore> { error("…") }` les basta.
- Hilt no encuentra `TokenStore` en `HiltTest` o `DeviceTest`: lo diste en `AppModule`, y esos tests lo cambian por `FakeModule`. Va en un módulo aparte.
- `KeyStoreException` o `AndroidKeyStore not found` en Robolectric: algo pidió el `TokenStore` de verdad. En los tests de la JVM, uno con `KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM, AeadConfig2026.get())`.
- StrictMode avisa de disco al arrancar: ¿abres el Keystore al crear `MainActivity`? `Lazy`, y `get()` dentro de `withContext(Dispatchers.IO)`.
- `theServerIsPinned` no encuentra los pines con el XML hecho: ¿el dominio es exactamente `api.bitacora.example`? El test busca ese texto.
- `theTokenStaysOutOfBackups` espera tres `exclude`: `cloud-backup`, `device-transfer` y `full-backup-content`.
- La sincronización falla en el emulador: `api.bitacora.example` no existe. Lo que se prueba sin servidor son la cabecera (con `MockEngine`) y el pinning (teoría 03, apartado 2).

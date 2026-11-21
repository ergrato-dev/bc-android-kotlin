# Rúbrica de Evaluación — Semana 38: Seguridad

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora segura |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿De qué protege el Keystore, y de qué no? ¿Por qué no basta cifrar el token con una clave guardada en otro archivo de la app? ¿Qué devuelve `getKey("bitacora_token", null).encoded`? | 10 |
| 2 | ¿Qué dos cosas hace AEAD? ¿Para qué sirven los datos asociados? ¿De dónde salen los 33 bytes de más en lo que cifra Tink? ¿Por qué no se repite un nonce, y quién lo elige? | 10 |
| 3 | ¿Qué es el cifrado envolvente y por qué existe? ¿Cómo se rota una clave en Tink sin perder lo cifrado antes? ¿Qué cambió en la API de Tink 1.24 respecto al registro global? | 10 |
| 4 | ¿Por qué `EncryptedSharedPreferences` no es la respuesta hoy? ¿Qué recomienda Tink en su lugar para un token, y para muchos datos? | 10 |
| 5 | ¿Qué tres partes tiene una Network Security Config? ¿Qué pasa si la app pide `http://` con `cleartextTrafficPermitted="false"`? ¿Por qué las CA de la persona solo en debug, y qué hace `overridePins`? | 10 |
| 6 | ¿Qué se fija, y cómo se calcula un pin? ¿Por qué dos pines, y por qué la de reserva no tiene certificado? ¿Qué pasa tras `expiration`? ¿Protege el pinning tu API de quien tiene root? | 10 |
| 7 | ¿Qué componentes exporta la release de la Bitácora y por qué los de las bibliotecas no son un problema? ¿Qué podía hacer otra app con el starter del ejercicio 02? ¿Qué dos cosas hace la Bitácora con lo que llega por `bitacora://pair`, y por qué la confirmación? | 10 |
| 8 | ¿Qué dos propiedades tiene que tener el `PendingIntent` de una notificación, y qué pasa con uno mutable e implícito? ¿Qué encontró lint en el starter del ejercicio 02 y qué no? | 10 |
| 9 | ¿Por qué no hay secretos en un APK? ¿Qué claves pueden ir en él? ¿De dónde llega la clave de Maps al build, y qué haces si una clave llega a git? ¿Por qué el token no va en las copias y las observaciones sí? | 10 |
| 10 | ¿Qué detecta la verificación de dependencias y qué un escáner? ¿Cuántos avisos encontró OSV en la Bitácora, y cuántos llegan al APK? ¿Qué da Play Integrity y por qué se comprueba en el servidor? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. De que la clave se copie: se crea dentro, se usa desde fuera y no va en copias ni a otro móvil. No de que tu propia app (o quien la controle con root) le pida descifrar. Si la clave está en un archivo, quien se lleva la carpeta se lleva las dos. `null`: no hay bytes que leer (medido en `DeviceTest`).
2. Cifra y autentica: un bit cambiado y descifrar falla. Los datos asociados no se cifran pero se comprueban: un texto cifrado para otra cosa no se abre como token. 5 de prefijo de Tink (versión e id de la clave), 12 de nonce y 16 de etiqueta (el token de 23 bytes ocupa 56 con una clave de Tink; con la del Keystore, 51, sin prefijo: medido). Con GCM, repetir nonce deja ver la combinación de dos textos y permite falsificar; lo elige Tink, al azar.
3. Una clave cifra los datos y otra (la maestra, en el Keystore) cifra esa clave: el Keystore es lento y admite pocas operaciones a la vez. `KeysetHandle.newBuilder(handle)` con una entrada nueva y `makePrimary()`: las de antes se quedan para descifrar, y el prefijo dice cuál usar. En 1.24, `generateNew` sin configuración, `AeadConfig.register()` y los `parse…` sin configuración están obsoletos: se pasa `AeadConfig2026.get()` a cada llamada (y al builder, o falla con `no key creator`).
4. `EncryptedSharedPreferences` y `MasterKey` están `@Deprecated` en `security-crypto` 1.1.0, que usa Tink 1.8.0 por dentro, y su archivo cifrado viajaba en las copias sin la clave. Tink desaconseja `AndroidKeysetManager`: para un token, `AndroidKeystore.getAead(alias)` directamente; para muchos datos, un keyset de Tink con esa `Aead` como maestra.
5. `base-config`, `domain-config` y `debug-overrides` (solo si es depurable). `Cleartext HTTP traffic to example.com not permitted`, antes de abrir el socket (medido). Quien instale una CA en el móvil leería el tráfico de la release; en debug hace falta para un proxy. `overridePins` deja que esas CA pasen sin pines: el proxy no tiene la clave del servidor.
6. La clave pública (SPKI), no el certificado, que se renueva. `openssl x509 -pubkey … | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64`. Si el servidor cambia de clave, las versiones instaladas solo aceptan sus pines: la reserva ya va dentro. No tiene certificado porque no la usa nadie todavía. Tras `expiration`, los pines no se comprueban, solo la CA. No: con root se quita en tiempo de ejecución; protege a la persona de un tercero en la red.
7. `MainActivity` sin permiso, `SystemJobService` con `BIND_JOB_SERVICE`, `DiagnosticsReceiver` y `ProfileInstallReceiver` con `DUMP` (medido): solo los usan el sistema y `adb`. Otra app abría `DetailActivity` con el código que quisiera y publicaba una alerta falsa por `AlertReceiver` (medido). Valida la forma (`[A-Za-z0-9_-]{20,200}`, esquema y host) y pide confirmación: sin ella, una página vincularía el móvil con su cuenta y se llevaría las observaciones.
8. Explícito (la clase de destino) e inmutable (`FLAG_IMMUTABLE`). Mutable e implícito: otra app que lo consiga lo redirige con la identidad de la tuya; desde Android 14 crearlo falla sin `FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT`. Lint encontró solo `ExportedReceiver` en la categoría Security; no vio la actividad exportada, el `PendingIntent`, `usesCleartextTraffic` ni la clave (medido).
9. El APK es un zip: `apkanalyzer` saca la clave pasada por propiedad (medido). Las que son públicas por diseño y se restringen en su consola (Maps, a paquete y firma) o identifican la app, no las de un servicio de pago. De `providers.gradleProperty("mapsApiKey")`, en `~/.gradle/gradle.properties` o en CI. Revocarla y crear otra; reescribir el historial no basta. El token lo cifra una clave que no viaja: en otro móvil no se abre. Las observaciones son de la persona y tienen que seguirla.
10. La verificación detecta un archivo cambiado; un escáner, un aviso publicado sobre una versión. 927 componentes, 11 con avisos; APK_ANSWER Play Integrity da un veredicto firmado sobre la app, el dispositivo y la cuenta; en la app, quien la modifica quita el `if`.

**Ejercicio 01, paso 6**: (a) 18 bytes de token, 51 de `seal`: 33 más (5 de prefijo, 12 de nonce, 16 de etiqueta). (b) `encryptedKeyset` en Base64 y `keysetInfo`, con el id de la primaria, el tipo (`AesGcmKey`), `ENABLED` y `TINK`; la clave, no. (c) Tres claves, todas `ENABLED`; la primaria es la última. (d) `GeneralSecurityException: decryption failed`: el keyset solo se abre con el contexto con que se cifró, y no se confunde con otro cifrado con la misma maestra.

**Ejercicio 02, paso 8**: (a) `ak_live_PRUEBA`: lo que va en el APK es público. (b) `res/8G.xml` en la release; `apkanalyzer` pierde el texto de los nodos (los pines, el dominio), `aapt2` lo enseña como `T: '…'`. (c) `java.io.IOException: Cleartext HTTP traffic to example.com not permitted`; y `javax.net.ssl.SSLHandshakeException: Pin verification failed`. (d) Solo `ExportedReceiver`: lint es un mínimo, no una revisión.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Cifrar con Tink (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 9 de 9 en verde | 13 |
| AEAD de Tink con `AES256_GCM`; el keyset guardado cifrado con la maestra y un contexto propio; sin APIs obsoletas; `open` solo traga `GeneralSecurityException`; `./gradlew build` en verde (revisión) | 3 |
| `NOTAS.md`: paso 6 explicado, con los bytes de (a) y el error de (d) | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Una app expuesta (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 13 |
| Solo el lanzador exportado; `PendingIntent` explícito e inmutable; configuración de red sin claro, con dos pines y caducidad, y las CA de la persona solo en debug; la clave por propiedad de Gradle (revisión) | 3 |
| `NOTAS.md`: paso 8 explicado; el paso 7 en un dispositivo hecho o dicho que no | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora segura

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings de Kotlin ni errores de lint, con la caché de configuración activada | 10 |
| Tests | `SecurityTest` y los de la semana 37 en verde (los que dejaron de compilar, arreglados sin debilitarlos), y mínimo 5 nuevos con el valor esperado escrito a mano (paso 10) | 25 |
| El token | Cifrado con AEAD y una clave del Keystore, en un módulo aparte, abierto fuera del hilo principal; `null` si no se puede abrir; como `Bearer` en cada petición; el enlace validado y confirmado | 25 |
| La red y las copias | Sin claro; `api.bitacora.example` fijado con dos pines y caducidad; CA de la persona solo en debug; `token.bin` fuera de las copias, y nada más | 15 |
| Adaptación al dominio | README con lo que pide el paso 11: el Keystore, las copias, los pines y su plan, lo exportado, el escaneo (o por qué no hay dispositivo) | 15 |
| Entrega | Rama `week-38`, pull request con la salida de `test` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| El token en claro, o la clave junto a él | -20 |
| El enlace guarda el token sin confirmar | -15 |
| `cleartextTrafficPermitted="true"` o CA de la persona en `base-config` | -15 |
| Un solo pin, o sin `expiration` | -10 |
| `allowBackup="false"` en lugar de excluir el token | -10 |
| Una clave, un token o un certificado privado en el repositorio | -20 |
| `EncryptedSharedPreferences` o APIs obsoletas de Tink | -10 |
| Un test de la semana 37 o del starter borrado o debilitado para que pase | -20 |
| Un sha256 añadido a mano, o un componente nuevo en la verificación sin revisar | -20 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-38` → `main` en el repositorio del aprendiz.

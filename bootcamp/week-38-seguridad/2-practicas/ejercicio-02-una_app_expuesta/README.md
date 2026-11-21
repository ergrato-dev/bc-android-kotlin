# Ejercicio 02 — Una app expuesta

⏱️ 2 h · Teoría previa: 02, 03, 04 y 05

## Objetivo

*Alertas* avisa de plagas en el campo: pide las alertas a su servidor y enseña cada una como notificación. Funciona, y tiene la puerta abierta por seis sitios. Al terminar:

- solo se entra desde el lanzador: el detalle y el receptor de alertas no están exportados;
- la notificación abre el detalle con un `PendingIntent` explícito e inmutable;
- la app no habla en claro con nadie, y con su servidor solo si su clave es una de las dos fijadas;
- la debug acepta un proxy para mirar el tráfico, y la release no;
- la clave del servidor no está en el repositorio;
- lint no encuentra ningún problema de seguridad.

Los tests no necesitan un dispositivo: miran los APK, el informe de lint y el `PendingIntent` en Robolectric. Si tienes uno (o KVM), el paso 7 lo comprueba como lo haría otra app.

## Cómo está montado

`starter/` (`dev.bitacora.alertas`) tiene un módulo, sin Compose ni AndroidX:

- `app/src/main/AndroidManifest.xml`: `MainActivity` (el lanzador), `DetailActivity` (el detalle de una alerta) y `AlertReceiver` (recibe una alerta y la enseña).
- `Alerts.kt`: `openIntent` (el `PendingIntent` de la notificación), `notifyAlert` y `fetchAlerts` (pide las alertas con la clave en `X-Api-Key`).
- `res/values/strings.xml`: los textos, y la clave del servidor.
- `server/alertas.pem`: el certificado del servidor de alertas. `server/backup.pem`: la clave pública de reserva.
- El raíz, con `src/test/kotlin/ExposureTest.kt` (6 tests), y `app/src/test/.../AlertsTest.kt` (2, en Robolectric). `./gradlew test` construye antes la release, la debug y el informe de lint de la release. No los toques.

Necesita `ANDROID_HOME`, con `build-tools;36.0.0`. Compila, y al empezar fallan los 8.

## Pasos

1. **Mira por fuera** (teoría 04): `./gradlew :app:assembleRelease` y `apkanalyzer manifest print` del APK. ¿Qué se exporta? ¿Hace falta? `./gradlew :app:lintRelease`: ¿cuántos de los problemas de este enunciado encuentra lint?
2. **Las puertas** (teoría 04, apartado 1): cierra lo que no tiene que estar abierto. Si sobra un `intent-filter`, quítalo. ¿Cómo llega la alerta de prueba de `MainActivity` a un receptor no exportado? Pasan `onlyTheLauncherIsExported` y `lintFindsNoSecurityIssue`.
3. **El `PendingIntent`** (teoría 04, apartado 3): explícito e inmutable. Pasan los dos de `AlertsTest`.
4. **La red** (teoría 02): `res/xml/network_security_config.xml`, conectado en el manifiesto. Nada en claro; el servidor por `https`; las CA de la persona solo en la debug. Fuera `usesCleartextTraffic`. Pasan `cleartextIsForbidden` y `userCertificatesOnlyInDebug`.
5. **Los pines** (teoría 03): calcula con `openssl` el pin de `server/alertas.pem` y el de `server/backup.pem`. Un `domain-config` para `alertas.bitacora.example` con los dos y una fecha de caducidad. Pasa `theServerIsPinned`.
6. **La clave** (teoría 05, apartado 2): fuera de `strings.xml`. Que llegue de la propiedad de Gradle `alertsKey`, vacía si no está, al mismo recurso: el código no cambia. Pasa `theKeyIsNotInTheRepository`. `./gradlew test`: 8 de 8. Después, `./gradlew build`.
7. **Si tienes un dispositivo o KVM**: instala el starter (`git stash`, o una copia) y, desde `adb shell`, haz de otra app: `am start -n dev.bitacora.alertas/.DetailActivity --es code FALSA` y `am broadcast -n dev.bitacora.alertas/.AlertReceiver --es code X-9 --es text falsa`. ¿Qué pasa? Repite con tu solución.
8. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) `./gradlew :app:assembleRelease -PalertsKey=ak_live_PRUEBA` y `apkanalyzer resources value --type string --config default --name alerts_key` del APK. ¿Qué sale? ¿Qué dice eso de las claves en una app?
   - (b) `apkanalyzer resources xml` y `aapt2 dump xmltree` de tu configuración de red en la release. ¿Cuál es su ruta? ¿Qué enseña uno que no enseña el otro?
   - (c) Con un dispositivo: cambia la URL a `http://example.com/` y pulsa *Actualizar*. ¿Qué mensaje sale? Después, pon `www.google.com` en tu `domain-config` y la URL `https://www.google.com/`. ¿Y ahora?
   - (d) Lint, en el paso 1: ¿qué encontró y qué no? ¿Basta lint para revisar la seguridad de una app?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `defaultConfig contains custom resource values, but the feature is disabled.` | `resValue` necesita `buildFeatures { resValues = true }` en AGP 9 |
| `No hay network_security_config` con el XML hecho | Falta `android:networkSecurityConfig="@xml/network_security_config"` en `<application>` |
| `theServerIsPinned`: los pines no coinciden | El pin es de la clave, no del certificado: `-pubkey` y `pkey -pubin -outform der` (teoría 03, apartado 1) |
| La alerta de prueba ya no aparece | El receptor no exportado solo recibe `Intent` de la app: `setPackage(packageName)` ya lo hace; ¿quitaste la acción del filtro? |
| `Path is not a file: …android-all-instrumented-6.0.1…` | Robolectric no encontró el `targetSdk`: `isIncludeAndroidResources` está en `app/build.gradle.kts`, no lo quites |
| `aapt2` no se encuentra | Los tests usan `build-tools/36.0.0`: instálalo con `sdkmanager "build-tools;36.0.0"` |

## Entrega

Los 8 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con el paso 8 (y el 7, si lo hiciste).

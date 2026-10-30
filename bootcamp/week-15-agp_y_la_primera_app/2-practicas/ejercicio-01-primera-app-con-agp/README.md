# Ejercicio 01 — La primera app con AGP

⏱️ 2 h · Teoría previa: 01, 02 y 03

## Objetivo

Convertir un módulo vacío en una aplicación Android que se instala: el bloque `android { }`, el manifiesto con su actividad de arranque y un APK que puedes inspeccionar con `apkanalyzer`, sin abrir Android Studio.

## Cómo está montado

`starter/sandbox/` es un build con un solo módulo, `:app`. El raíz ya declara AGP 9.4.1 y Kotlin 2.4.21 con `apply false`, y `settings.gradle.kts` ya tiene `google()`. `MainActivity` (ya hecha) pinta un texto. Los tests (`src/test/kotlin/BuildTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal con la clase `Sandbox`, construyen el APK y le preguntan a `apkanalyzer` (clase `Apk`) por su contenido. Todos empiezan en rojo: sin `compileSdk`, AGP no llega ni a configurar `:app`.

Necesitas el SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../../../docs/setup.md)). Para probarlo a mano: `./gradlew -p sandbox assembleDebug` y después `apkanalyzer manifest print sandbox/app/build/outputs/apk/debug/app-debug.apk`.

## Pasos

1. **El bloque `android { }`** (`sandbox/app/build.gradle.kts`): `namespace = "dev.bitacora"` y `compileSdk = 37`.
2. **`defaultConfig { }`**, dentro: `applicationId = "dev.bitacora.agro"`, `minSdk = 26`, `targetSdk = 36`, `versionCode = 1` y `versionName = "1.0.0"`.
3. **El manifiesto** (`sandbox/app/src/main/AndroidManifest.xml`): `android:label="Bitácora"` en `<application>` y, dentro, la actividad `.MainActivity` con `android:exported="true"` y el `intent-filter` con la acción `MAIN` y la categoría `LAUNCHER`.
4. **Ejecuta** `./gradlew test` (7 de 7).
5. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `android:exported` y ejecuta `./gradlew -p sandbox assembleDebug`. Copia la primera línea del error. ¿Por qué lo exige `targetSdk 36` y no lo exigía Android 11? Restáuralo.
   - (b) Añade `id("org.jetbrains.kotlin.android")` (sin versión: ya está en el classpath) a los `plugins` de `app`. ¿Qué dice el error? Quítalo.
   - (c) Ejecuta `./gradlew -p sandbox buildEnvironment` y busca `kotlin-gradle-plugin`. ¿Qué versión trae AGP, cuál se usa y qué línea del build lo decide?
   - (d) `apkanalyzer files list` sobre el APK de debug: ¿cuántos `classes*.dex` hay y dónde está tu `MainActivity`? (`apkanalyzer dex packages`).
   - (e) Opcional, si tienes un móvil con la depuración USB activada: `adb install -r sandbox/app/build/outputs/apk/debug/app-debug.apk` y ábrela desde el lanzador. Apunta qué ves.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| ``project ':app' does not specify `compileSdk` `` | Paso 1: falta `compileSdk`, o está fuera de `android { }` |
| `Namespace not specified` | Paso 1: falta `namespace` |
| `SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable` | `ANDROID_HOME` no está definida en la terminal desde la que lanzas `./gradlew` ([setup](../../../../docs/setup.md)). Si el daemon arrancó sin ella, `./gradlew --stop` |
| `Failed to find Platform SDK with path: platforms;android-37.0` | Falta la plataforma: `sdkmanager "platforms;android-37.0"` |
| `Manifest merger failed : android:exported needs to be explicitly specified` | Paso 3: falta `android:exported` en la actividad |
| `theActivityIsInTheLauncher` falla y el APK se construye | El `intent-filter` no está dentro de `<activity>`, o falta la acción o la categoría |
| `Define ANDROID_HOME con la carpeta del SDK de Android` en un test | Lo lanza `Apk` (el test, no el build): define la variable y vuelve a ejecutar |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.

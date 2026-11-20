# Ejercicio 02 — Una biblioteca y una fuga

⏱️ 2 h · Teoría previa: 01, 03, 05 y 06

## Objetivo

Una app de Compose que muestra las lecturas de sus sondas, y una biblioteca, `:sensors`, que las crea por el nombre de su clase y avisa a quien se apunte. Al terminar:

- la release pasa por R8, con código y recursos;
- la regla que necesita la reflexión de `:sensors` viaja en su AAR, y la app no sabe nada de ella;
- LeakCanary vigila la debug y sus tests del dispositivo, y has encontrado y arreglado la fuga que tiene la app.

Los tests no necesitan un dispositivo: miran el AAR, los APK y el mapping. Si tienes uno (o KVM para un emulador), el paso 6 encuentra la fuga de verdad.

## Cómo está montado

`starter/` tiene dos módulos (`dev.bitacora.sensores`):

- `sensors/`: una biblioteca de Android con `Probe` y `Sensors`, ya hecha. `Sensors.load` crea las sondas con `Class.forName`; `subscribe`, `unsubscribe` y `publish` avisan de cada lectura.
- `app/`: `Humidity` y `Temperature` (`Probes.kt`), sus nombres en `res/values/strings.xml` (`probes`), `MainActivity`, que se apunta a `Sensors` y muestra las lecturas, y `res/drawable/old_logo.xml`, que nadie usa. En `src/androidTest`, `LeakTest`, que abre y cierra la actividad.
- `gradle/libs.versions.toml`, con LeakCanary 2.14.
- El raíz, con `src/test/kotlin/ShrinkTest.kt`: 6 tests. `./gradlew test` construye antes el AAR, la app en release y en debug, y el APK de sus tests del dispositivo. No los toques.

Necesita `ANDROID_HOME`. Compila, y al empezar fallan 5 de 6.

## Pasos

1. **R8 en la release** (teoría 01, apartado 2): en `app/build.gradle.kts`, `isMinifyEnabled`, `isShrinkResources` y las reglas de AGP más `proguard-rules.pro`. `./gradlew :app:assembleRelease`. ¿Falla? Lee el mensaje y crea lo que falta, con un comentario que diga qué va en él. Pasa `theReleaseGoesThroughR8` y `theOldLogoIsGone`.
2. **Lo que se ha perdido**: busca `Humidity` en `app/build/outputs/mapping/release/mapping.txt` y en `usage.txt`. ¿Dónde está? ¿Qué pasaría al abrir la release? Falla `theProbesKeepTheirNames`.
3. **La regla, con la biblioteca** (teoría 03, apartado 2): en `sensors/`, un `consumer-rules.pro` con la regla de las sondas, y `consumerProguardFiles` en su `build.gradle.kts`. Ni una línea en el `proguard-rules.pro` de la app. Abre `sensors/build/outputs/aar/sensors-release.aar` (es un zip): ¿dónde está tu regla? Pasan `theLibraryShipsItsRule` y `theProbesKeepTheirNames`.
4. **Medir** (teoría 05): `apkanalyzer apk file-size` y `download-size` de la release, y `apkanalyzer dex references`. Desactiva R8 un momento, repite y apunta los números. ¿Cuánto es de la app, y cuánto de Compose? (`dex packages` con `--proguard-mappings`.)
5. **LeakCanary**: en la debug, y en los tests del dispositivo (`leakcanary-android-instrumentation`). Pasa `leakCanaryIsOnlyInDebug` cuando además hagas el paso 6.
6. **La regla de las fugas** (teoría 06, apartado 3): `DetectLeaksAfterTestSuccess` en `LeakTest`. `./gradlew test`: 6 de 6. Después, `./gradlew build`.

   **Si tienes un dispositivo o KVM**: `./gradlew connectedDebugAndroidTest`. Falla. Lee el informe de abajo arriba: ¿qué objeto se fuga? ¿Quién lo retiene, y qué está subrayado? Arréglalo en `MainActivity` (lo que se apunta en `onCreate`, se borra en `onDestroy`) y vuelve a ejecutarlo hasta que pase.
7. **`NOTAS.md`** (en `starter/`). Haz cada cambio, ejecuta, apunta lo que pasa y restáuralo:
   - (a) Quita `isMinifyEnabled` y deja `isShrinkResources`. ¿Qué dice Gradle?
   - (b) Pon `android.enableR8.fullMode=false` en `gradle.properties`. ¿Qué avisa AGP? ¿Cuánto ocupa ahora el APK de la release? ¿Por qué más?
   - (c) Mueve tu regla al `proguard-rules.pro` de la app. ¿Funciona la release? ¿Qué test falla, y por qué es mala idea aunque funcione?
   - (d) Si hiciste el paso 6 en un dispositivo: ¿cuántos bytes retenía la fuga? ¿Cuánto tardó el test con la fuga y sin ella?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Supplied proguard configuration does not exist: .../app/proguard-rules.pro` | Lo nombras en `proguardFiles` y no existe: créalo |
| `Removing unused resources requires unused code shrinking to be turned on` | `isShrinkResources` sin `isMinifyEnabled` |
| El AAR no tiene `proguard.txt` | `consumerProguardFiles` falta, o el archivo está en otra carpeta: la ruta es relativa a `sensors/` |
| `Unresolved reference 'DetectLeaksAfterTestSuccess'.` en `LeakTest` | Falta `androidTestImplementation(libs.leakcanary.android.instrumentation)` |
| `leakcanary.NoLeakAssertionFailedError: Application memory leaks were detected` | Es lo que buscas en el paso 6: hay una fuga. Lee el informe |
| `aapt2` no se encuentra | Los tests usan `build-tools/36.0.0`: instálalo con `sdkmanager "build-tools;36.0.0"` |

## Entrega

Los 6 tests en verde, `./gradlew build` en verde, y `NOTAS.md` con las respuestas del paso 7 (y del paso 6 en un dispositivo, si lo hiciste).

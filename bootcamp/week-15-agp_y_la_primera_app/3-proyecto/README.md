# Proyecto — La Bitácora, app Android

⏱️ 4 h · Capa 14 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md). Abre la fase 3.

## Qué construyes

La Bitácora deja de ser un programa de consola: `app` es una aplicación Android que se construye, se verifica y se instala. Por dentro casi nada cambia. `core` y `data` siguen siendo Kotlin puro, `build-logic` gana un convention plugin, `bitacora.android-application`, y `bitacora.build-info` aprende a dejar su archivo en los assets de cada variante. La verificación, la caché de configuración y el paralelo de la semana 14 siguen activos, ahora con AGP dentro.

## Dónde trabajas

En **tu repositorio**, rama `week-15`, a partir de lo que entregaste en la semana 14. El `starter/` es la Bitácora de la semana 14 resuelta, con comentarios `Paso N` donde va cada cambio. En `src/test/kotlin/` hay una clase nueva, `Apk` (ya hecha), que pregunta a `apkanalyzer` por un APK, y en `BuildTest.kt`, un test de ejemplo que empieza en rojo. Copia eso a tu repositorio.

Necesitas el SDK de Android y `ANDROID_HOME` ([docs/setup.md](../../../docs/setup.md)).

## Pasos

1. **El catálogo** (`gradle/libs.versions.toml`): la versión `agp = "9.4.1"`, la biblioteca `android-gradle-plugin` (`com.android.tools.build:gradle`) y el plugin `android-application` (`com.android.application`).
2. **Los repositorios** (`settings.gradle.kts`): `google()` y `gradlePluginPortal()` en `pluginManagement { repositories { } }`, y `google()` también en `dependencyResolutionManagement`.
3. **El raíz** (`build.gradle.kts`): `alias(libs.plugins.android.application) apply false`. Sin esta línea, `app` falla al aplicar AGP (teoría 04).
4. **`build-logic`**: `implementation(libs.android.gradle.plugin)` en su `build.gradle.kts` y `google()` en su `settings.gradle.kts`.
5. **El convention plugin** `build-logic/src/main/kotlin/bitacora.android-application.gradle.kts`: aplica `com.android.application`, configura `android { }` (`namespace = "dev.bitacora"`, `compileSdk = 37`, `minSdk = 26`, `targetSdk = 36`, `versionCode = 1`, `versionName` = la versión del proyecto), Kotlin como en `bitacora.kotlin-base` (toolchain 21, warnings como errores) y aplica `BuildInfoPlugin`.
6. **`bitacora.build-info` en Android** (`BuildInfoPlugin.kt`): con `pluginManager.withPlugin("com.android.application")`, pide la extensión `ApplicationAndroidComponentsExtension` y, en `onVariants`, conecta la tarea a `variant.sources.assets` con `addGeneratedSourceDirectory`. La rama de `java` se queda: `bitacora.kotlin-application` sigue funcionando.
7. **`app` es Android**: `id("bitacora.android-application")`, `applicationId = "dev.bitacora.<tu dominio>"` en `android { defaultConfig { } }`, `AndroidManifest.xml` con la actividad de arranque, `res/values/strings.xml` con `app_name` (y `android:label="@string/app_name"`), y `MainActivity` en el paquete `dev.bitacora` en lugar de `Main.kt`: lee `build-info.properties` de los assets y enseña lo mismo que imprimía `main`. Para importarlos desde un paquete, `core` y `data` pasan a `dev.bitacora.core` y `dev.bitacora.data` (código y tests).
8. **La verificación**: `./gradlew --write-verification-metadata sha256 --no-build-cache clean help assembleDebug assembleRelease`. Comprueba que el archivo tiene `com.android.tools.build:gradle` y `aapt2`, y que `./gradlew build` sigue en verde **con** la caché de configuración.
9. **Mide**: `./gradlew clean assembleDebug --no-build-cache --profile` dos veces y, después, cambia una línea de `core` y ejecuta `./gradlew :app:assembleDebug`. Apunta los tres tiempos y el número de tareas en tu README, junto a los de la semana 14.
10. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
    - el `applicationId` del APK es el de tu dominio,
    - `MainActivity`, una clase de `core` y otra de `data` están en el dex,
    - el APK de release también lleva `assets/build-info.properties`,
    - con `version=1.1.0` en `gradle.properties` cambian el asset y el `version-name` del manifiesto,
    - AGP alterado en la verificación (`tamper("gradle-9.4.1.jar")`) hace fallar hasta `help`,
    - `:app:assembleDebug` dos veces: la segunda dice `Configuration cache entry reused.` y `generateBuildInfo` está `UP_TO_DATE`,
    - `:core:test` no ejecuta ninguna tarea de Android.
11. **Explica en tu README** qué ha cambiado en `core` y `data` (y qué no), y por qué `aapt2` no aparece en la verificación si no hay recursos en `res/`.
12. **Abre el pull request** `week-15` → `main` con la salida de `./gradlew test`, tus mediciones y el número de componentes de `gradle/verification-metadata.xml` antes y después.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings de Kotlin, la configuración de Android en el convention plugin y no en `app`, `core` y `data` sin Android, verificación regenerada por Gradle con `sha256`, caché de configuración activa, y tests del build con valores calculados a mano.

## Pistas

- `withPlugin` ejecuta su bloque cuando se aplica el plugin, antes o después: así `BuildInfoPlugin` no depende del orden en que se apliquen.
- En el convention plugin, el bloque `android { }` de `com.android.application` se escribe igual que en un `build.gradle.kts`; el `applicationId` va en `app` porque es lo único que cambia de una app a otra.
- `apkanalyzer manifest print` y `apkanalyzer dex packages` son tus mejores amigos para escribir los tests: mira la salida real y escribe el valor esperado a mano.
- Si `./gradlew build` falla con `Configuration cache state could not be cached` y, más abajo, `Dependency verification failed`, falta un artefacto en la verificación: repite el paso 8 tal cual, con `--no-build-cache clean`.
- `lint` (dentro de `build`) avisa de que la app no tiene icono (`MissingApplicationIcon`). Es un aviso, no un error: el icono llega con los recursos, más adelante en la fase 3.
- Si Gradle avisa de que el daemon se queda sin `Metaspace`, sube la memoria en `gradle.properties` (`org.gradle.jvmargs`) y apunta en tu README el antes y el después.

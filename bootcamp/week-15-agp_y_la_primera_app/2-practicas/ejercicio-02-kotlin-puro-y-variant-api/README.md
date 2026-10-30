# Ejercicio 02 — Kotlin puro y la Variant API

⏱️ 2 h · Teoría previa: 03 y 05

## Objetivo

Que la app use un módulo de Kotlin puro, `core`, que se prueba en la JVM sin Android. Y llevar la tarea `generateBuildInfo` de la semana 9 a Android: su salida entra en los assets de cada variante (`debug` y `release`) con la Variant API, sin escribir a mano ninguna ruta de `build/`.

## Cómo está montado

`starter/sandbox/` tiene dos módulos. `:app` es la app del ejercicio 01, ya resuelta; su `MainActivity` lee `build-info.properties` de los assets y saluda con `greeting()`, de `core`. `:core` es Kotlin puro (`kotlin.jvm`), con su test. En `app/build.gradle.kts` está la clase `GenerateBuildInfo` (ya hecha). Los tests (`src/test/kotlin/BuildTest.kt`, no los toques) construyen los APK y los inspeccionan con `apkanalyzer`. Uno pasa desde el principio, `coreTestsRunOnTheJvm`: `core` ya es Kotlin puro y tiene que seguir siéndolo.

## Pasos

1. **`app` usa `core`** (`sandbox/app/build.gradle.kts`): `implementation(project(":core"))`.
2. **Registra la tarea** `generateBuildInfo`, de tipo `GenerateBuildInfo`, con `appVersion` = la versión del proyecto y `domain = "agro"`. No le des `outputDir`.
3. **Conéctala a las variantes**: `androidComponents { onVariants { variant -> ... } }` y, dentro, `variant.sources.assets?.addGeneratedSourceDirectory(tarea, GenerateBuildInfo::outputDir)`.
4. **Ejecuta** `./gradlew test` (7 de 7).
5. **`NOTAS.md`** (en `starter/`):
   - (a) ¿En qué carpeta deja la tarea `build-info.properties`? ¿Quién la ha elegido? ¿Por qué no hay una tarea por variante (`generateDebugBuildInfo`)?
   - (b) Ejecuta `./gradlew -p sandbox :core:test` y después `./gradlew -p sandbox :app:testDebugUnitTest`, con `--profile` las dos. ¿Cuánto tarda cada una y cuántas tareas ejecuta? ¿Qué ganas con que `core` no sepa nada de Android?
   - (c) ¿Por qué el APK de release se llama `app-release-unsigned.apk`? ¿Se puede instalar? (la firma llega al final de la fase 3).
   - (d) Cambia `onVariants { }` por `onVariants(selector().withBuildType("release")) { }`. ¿Qué tests fallan y por qué? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'greeting'` al compilar `app` | Paso 1: `app` no depende de `core` |
| `assets.open` lanza `FileNotFoundException` al abrir la app | La tarea no está conectada a la variante (paso 3) |
| `theOutputDirIsChosenByAgp` falla | Le diste `outputDir` a la tarea: quítalo, la ruta la pone AGP |
| `aNewVersionChangesTheAssetAndTheManifest` falla solo en el asset | `appVersion` está escrita a mano (`"1.0.0"`): usa la versión del proyecto |
| `coreTestsRunOnTheJvm` falla | Tocaste `core/build.gradle.kts`: `core` sigue siendo `kotlin.jvm` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.

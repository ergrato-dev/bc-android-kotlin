# Ejercicio 01 — Dos sabores

⏱️ 2 h · Teoría previa: 01, 02, 03 y 04

## Objetivo

Una app con dos flavors, `demo` y `field`. La demo se instala al lado de la de campo, se llama de otra forma, en los dos idiomas, arranca con observaciones de ejemplo y habla con otro servidor. Las diferencias están en el build, en carpetas de cada flavor y en `BuildConfig`. El código de `main` no pregunta en qué variante está.

## Cómo está montado

`starter/` es una app Android de un solo módulo, con Robolectric. `MainActivity` tiene un texto con el servidor (`server`) y una lista (`list`). `BuildConfig` ya está activado en `app/build.gradle.kts`. Los textos están en `res/values` y `res/values-en`.

Los tests (`app/src/test/kotlin/dev/bitacora/FlavorsTest.kt`, no los toques) se ejecutan una vez por variante. Cada uno lee en qué flavor está con `BuildConfig.FLAVOR`, por reflexión, porque sin flavors ese campo no existe (teoría 04). Sin flavors, los 6 fallan con `La app no tiene los flavors demo y field (FLAVOR = null)`.

## Pasos

1. **Los tipos de build** (`app/build.gradle.kts`): `debug` con `applicationIdSuffix = ".debug"` y `versionNameSuffix = "-debug"`.
2. **Los flavors**:
   - la dimensión `environment`;
   - `demo`, con `applicationIdSuffix = ".demo"` y `versionNameSuffix = "-demo"`;
   - `field`, con `isDefault = true`;
   - en los dos, `buildConfigField` `String` `SERVER_URL`: `https://demo.bitacora.example` en demo y `https://api.bitacora.example` en field. Ojo con las comillas (teoría 04).
3. **Sin `demoRelease`**: `androidComponents.beforeVariants` con `selector()`.
4. **Los ejemplos**:
   - `object Samples` con `val observations: List<String>` en `src/demo/kotlin/dev/bitacora/`, con tres observaciones;
   - el mismo objeto, vacío, en `src/field/kotlin/dev/bitacora/`;
   - en `MainActivity`, una fila (`TextView`) por cada una.
5. **El servidor**: `server` muestra `BuildConfig.SERVER_URL`.
6. **El nombre de la demo**: `Bitácora demo` y, en inglés, `Field log demo`. Pon solo `app_name` en las carpetas de recursos de `demo`.
7. **Ejecuta** `./gradlew test` (12 de 12, 6 por variante) y `./gradlew build`.
8. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `src/demo/res/values-en`. ¿Qué test falla, en qué variante y con qué mensaje? ¿Por qué gana `main` si `demo` tapa a `main`? Restáuralo.
   - (b) Mueve `Samples.kt` de `field` a `src/main/kotlin`. ¿Qué dice el compilador, y en qué variantes? Restáuralo.
   - (c) Quita `isDefault = true` y ejecuta `./gradlew :app:lint --dry-run`. ¿Qué variante mira lint? ¿Qué se quedaría sin revisar? Restáuralo.
   - (d) Ejecuta `./gradlew :app:lintDebug` y `./gradlew test --tests '*FlavorsTest'`. ¿Qué dice cada uno? ¿Cómo ejecutas solo los tests de field?
   - (e) Construye con `./gradlew :app:assembleDebug` y mira `app/build/outputs/apk/`. ¿Cuántos APK de debug hay? Comprueba el `applicationId` de cada uno con `apkanalyzer manifest application-id`. ¿Se podrían instalar los dos en el mismo móvil? ¿Y la de release a su lado?

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'Samples'` en un solo `compile...Kotlin` | Falta `Samples.kt` en la carpeta de ese flavor |
| `Redeclaration:` | `Samples` está en `main` y en un flavor: va solo en las de los flavors |
| `Unresolved reference 'SERVER_URL'` | El campo tiene que estar en los dos flavors |
| `BuildConfig.java:14: error: ';' expected`, en `compileFieldDebugJavaWithJavac` | El valor de un `String` lleva comillas dentro: `"\"https://...\""` |
| `All flavors must now belong to a named flavor dimension` | Falta `flavorDimensions += "environment"` |
| `expected:<Field log[ demo]> but was:<Field log[]>` | Falta `src/demo/res/values-en/strings.xml` |

## Entrega

Los 12 tests en verde, `./gradlew build` en verde y `NOTAS.md` con las respuestas del paso 8.

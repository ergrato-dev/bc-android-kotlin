# Ejercicio 02 — Version catalog y repositorios

⏱️ 2 h · Teoría previa: 03 y 04

## Objetivo

Migrar un build de tres módulos que ya funciona, con versiones escritas a mano, a un **catálogo de versiones** y a **repositorios centralizados**, sin romper nada.

## Cómo está montado

Como el ejercicio 01: tests en la raíz de `starter/` (no la toques) y el build en `sandbox/`. Aquí `sandbox/` **ya compila y funciona**: tiene las versiones repetidas y un `repositories { }` por módulo. Por eso 3 de los 7 tests pasan desde el principio (compila, se ejecuta, las versiones llegan al classpath): son tu **red de seguridad** y tienen que seguir pasando mientras migras. Para probarlo a mano, desde `starter/`: `./gradlew -p sandbox build`.

## Pasos

1. **`sandbox/gradle/libs.versions.toml`**: el catálogo. Versiones `kotlin` (2.4.21), `serialization` (1.12.0) y `junit` (6.1.3). Bibliotecas: `junit-bom`, `junit-launcher`, `kotlin-test`, `serialization-bom` y `serialization-json` (sin versión, la fija el BOM). *Bundle* `testing` con `kotlin-test` y `junit-launcher`. Plugins `kotlin-jvm` y `kotlin-serialization` (con `version.ref = "kotlin"`).
2. **Raíz**: los plugins con `alias(libs.plugins...)` y `apply false`.
3. **Módulos**: plugins con `alias`, `platform(libs.junit.bom)`, `libs.bundles.testing`, `libs.serialization.json`... Ninguna versión escrita a mano.
4. **Repositorios**: en `settings.gradle.kts`, `dependencyResolutionManagement` con `FAIL_ON_PROJECT_REPOS` y `mavenCentral()`; bórralos de los módulos.
5. **`NOTAS.md`** (en `starter/`): (a) Escribe mal un nombre (`libs.junit.bomm`) y ejecuta `:core:compileKotlin`: ¿en qué momento falla y qué dice? (b) Deja vacío el `build.gradle.kts` del raíz (sin los plugins) y ejecuta `build`: ¿qué avisa Gradle y qué arreglo propone? Restáuralo. (c) Deja un `repositories { mavenCentral() }` en un módulo: ¿qué dice Gradle? (d) Cuenta cuántos archivos habrías tenido que tocar para subir la versión de JUnit antes del catálogo, y cuántos ahora.
6. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference: libs` | El catálogo tiene que estar en `gradle/libs.versions.toml` (dentro de `sandbox/`) |
| `Unresolved reference` en `libs.serialization.json` | La entrada se llama `serialization-json` (guion) y se usa con puntos |
| `Invalid TOML catalog definition` | Revisa las comillas y las llaves; una biblioteca es `{ module = "...", version.ref = "..." }` |
| `The Kotlin Gradle plugin was loaded multiple times` | El raíz no declara el plugin: `alias(libs.plugins.kotlin.jvm) apply false` |
| `prefer settings repositories over project repositories` | Queda algún `repositories { }` en un módulo |
| El test de versiones dice que `2.4.21` aparece dos veces | Está en `[versions]` y también en `kotlin-test` o un plugin: usa `version.ref` |
| El test de literales falla | Queda alguna cadena como `"org.junit:junit-bom:6.1.3"` o `kotlin("jvm") version "..."` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.

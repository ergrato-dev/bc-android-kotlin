# Ejercicio 02 — Publicar y consumir

⏱️ 2 h · Teoría previa: 05

## Objetivo

Publicar el plugin del ejercicio 01 en un repositorio Maven de carpeta (`build/repo`) y usarlo desde otro build, `consumer/`, con su id y su versión, sin `withPluginClasspath` y sin el Plugin Portal.

## Cómo está montado

`starter/` es el ejercicio 01 resuelto, más la carpeta `consumer/`: un build aparte que quiere usar el plugin. Los tests (`src/test/kotlin/PublishTest.kt`, no los toques) copian todo a una carpeta temporal con la clase `Workspace` (ya hecha), ejecutan `publish` en el plugin y después Gradle en `consumer/`. Uno pasa desde el principio (`validatePlugins`): es la red de seguridad del plugin. Para probarlo a mano, desde `starter/`: `./gradlew publish` y después `./gradlew -p consumer processResources`.

## Pasos

1. **Publicar** (`build.gradle.kts`): el plugin `` `maven-publish` ``, `group = "dev.bitacora"`, `version = "1.0.0"` y un repositorio `maven` llamado `local` en `layout.buildDirectory.dir("repo")`.
2. **Repositorio del consumidor** (`consumer/settings.gradle.kts`): `pluginManagement { repositories { maven { url = uri("../build/repo") } } }`. Solo ese.
3. **Usar el plugin** (`consumer/build.gradle.kts`): `id("bitacora.build-info") version "1.0.0"` en `plugins { }` y `buildInfo { domain = "obra" }`.
4. **`NOTAS.md`** (en `starter/`):
   - (a) Abre el marcador (`build/repo/bitacora/build-info/.../bitacora.build-info.gradle.plugin-1.0.0.pom`). ¿Qué contiene y qué no? ¿Por qué hace falta si ya está el jar?
   - (b) Borra el bloque `pluginManagement` de `consumer/settings.gradle.kts` y ejecuta `./gradlew -p consumer processResources`. ¿En qué repositorio buscó Gradle? Restáuralo.
   - (c) Cambia la descripción de la tarea en `BuildInfoPlugin.kt`, vuelve a publicar **la misma** `1.0.0` y ejecuta `./gradlew -p consumer help --task generateBuildInfo`. ¿Qué descripción sale? ¿Qué problema tiene eso para quien ya usaba la `1.0.0`?
   - (d) `./gradlew publishToMavenLocal` publica en `~/.m2/repository`. ¿Por qué no se usa en este ejercicio? Piensa en qué otros builds de tu máquina leen esa carpeta.
5. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Task 'publish' not found in root project 'build-info-plugin'` | Falta `` `maven-publish` `` en `plugins { }` |
| `publish` dice `UP-TO-DATE` y no hay `build/repo` | Falta el repositorio en `publishing { repositories { } }`: sin destino, `publish` no hace nada y no avisa |
| `could not resolve plugin artifact 'bitacora.build-info:bitacora.build-info.gradle.plugin:1.0.0'` | No has publicado, o la `url` de `consumer/settings.gradle.kts` no apunta a `build/repo` (es relativa a `consumer/`) |
| `Searched in the following repositories: Gradle Central Plugin Repository` | `consumer/settings.gradle.kts` no tiene `pluginManagement` |
| El test del Portal falla | Has añadido `gradlePluginPortal()` al consumidor: solo hace falta tu repositorio |
| El jar está en otra carpeta | `group` o `rootProject.name` no son los del paso 1: las carpetas salen de las coordenadas |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 4.

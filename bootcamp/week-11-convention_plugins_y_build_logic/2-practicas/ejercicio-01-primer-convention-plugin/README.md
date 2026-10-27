# Ejercicio 01 — Primer convention plugin

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Sacar de los tres módulos la configuración que repiten (plugin de Kotlin, JDK, warnings como errores, JUnit) y meterla en **un** convention plugin, `bitacora.kotlin-base`, que vive en el build incluido `build-logic`.

## Cómo está montado

Como en la semana 10: los tests están en la raíz de `starter/` (no la toques) y el build que editas está en `sandbox/`. `sandbox/` es la Bitácora de la semana 10 **ya funcionando**, con la duplicación intacta, y una carpeta `build-logic/` a medio hacer (su `settings.gradle.kts` ya está). Por eso 4 de los 7 tests pasan desde el principio (el programa, el JDK 21, los warnings y los tests de los módulos): son tu **red de seguridad** y tienen que seguir en verde mientras mueves la configuración. Para probarlo a mano, desde `starter/`: `./gradlew -p sandbox build`.

## Pasos

1. **`sandbox/settings.gradle.kts`**: `pluginManagement { includeBuild("build-logic") }` al principio.
2. **`build-logic`**: añade al catálogo (`sandbox/gradle/libs.versions.toml`) la biblioteca `kotlin-gradle-plugin` con `version.ref = "kotlin"`, y escribe `build-logic/build.gradle.kts`: `kotlin-dsl` y `implementation(libs.kotlin.gradle.plugin)`.
3. **`bitacora.kotlin-base.gradle.kts`**: `id("org.jetbrains.kotlin.jvm")` (sin versión), el bloque `kotlin { }` y `tasks.test { }`.
4. **Módulos**: en los tres, `id("bitacora.kotlin-base")` en lugar de `alias(libs.plugins.kotlin.jvm)`; borra `kotlin { }` y `tasks.test { }`. Lo demás (`java-library`, `application`, las dependencias) se queda.
5. **`NOTAS.md`** (en `starter/`):
   - (a) Quita `alias(libs.plugins.kotlin.jvm) apply false` del `build.gradle.kts` raíz. Ejecuta `./gradlew -p sandbox build`, añade `description = "Bitácora"` al final de `bitacora.kotlin-base.gradle.kts` y ejecuta `./gradlew -p sandbox :core:compileKotlin --info`. Copia la línea que explica por qué `:core:compileKotlin` no está `UP-TO-DATE`. ¿Por qué cambió eso, si no tocaste `core`? Restaura las dos cosas.
   - (b) Escribe mal el id en `core` (`bitacora.kotlin-bse`). Copia el mensaje. ¿Cuáles son los tres sitios donde busca Gradle y cuál es el tuyo?
   - (c) Ejecuta `./gradlew -p sandbox build` dos veces. En la segunda, ¿en qué estado quedan las tareas `:build-logic:`?
   - (d) Si mañana el JDK pasa a 25, ¿cuántos archivos tocabas antes de este ejercicio y cuántos ahora?
6. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Plugin [id: 'bitacora.kotlin-base'] was not found` | Falta `includeBuild("build-logic")`, o el archivo no se llama `bitacora.kotlin-base.gradle.kts`, o no está en `build-logic/src/main/kotlin` |
| `Unresolved reference 'libs'` en `build-logic/build.gradle.kts` | El catálogo lo importa `build-logic/settings.gradle.kts` (ya está): revisa que no lo hayas tocado |
| `Unresolved reference 'gradle'` en `build-logic/build.gradle.kts` | Falta la entrada `kotlin-gradle-plugin` en el catálogo: el acceso es `libs.kotlin.gradle.plugin` |
| `Unresolved reference 'jvmToolchain'` en el plugin | Falta `id("org.jetbrains.kotlin.jvm")` en el `plugins { }` del plugin |
| El test de repetición dice que un módulo todavía tiene `plugins.kotlin.jvm` | Gradle no se queja si dejas `alias(libs.plugins.kotlin.jvm)` junto a la base, pero sobra: ya lo aplica el plugin |
| El test de recompilación falla | El raíz tiene que seguir declarando `alias(libs.plugins.kotlin.jvm) apply false` (paso 5a) |
| Los tests tardan | Es normal: cada test arranca Gradle, y el primero compila `build-logic` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 5.

# Ejercicio 02 — Catálogo y composición

⏱️ 2 h · Teoría previa: 03 y 04

## Objetivo

Encima de la base del ejercicio 01, crear un convention plugin por **tipo de módulo** (`bitacora.kotlin-library` y `bitacora.kotlin-application`) que saca las dependencias de prueba **del catálogo**, y dejar cada módulo con solo lo suyo.

## Cómo está montado

Igual que el ejercicio 01. `sandbox/` es el ejercicio 01 resuelto: `bitacora.kotlin-base` ya funciona, `core` y `data` siguen declarando sus dependencias de prueba, y los dos plugins nuevos son archivos con los pasos en comentarios. 4 de los 7 tests pasan desde el principio (el programa, las versiones solo en el catálogo, `app` como único programa y Kotlin cargado una vez): son la red de seguridad. Para probarlo a mano, desde `starter/`: `./gradlew -p sandbox build`.

## Pasos

1. **`bitacora.kotlin-library.gradle.kts`**: aplica `id("bitacora.kotlin-base")` y `` `java-library` ``; pide el catálogo con `val libs = versionCatalogs.named("libs")` y añade `testImplementation(platform(libs.findLibrary("junit-bom").get()))` y `testImplementation(libs.findBundle("testing").get())`.
2. **`bitacora.kotlin-application.gradle.kts`**: aplica la base y `application`. Nada más.
3. **Módulos**: `core` se queda en `plugins { id("bitacora.kotlin-library") }`. `data`: el mismo plugin, más serialization y sus dependencias. `app`: `id("bitacora.kotlin-application")`, su dependencia de `data` y su `mainClass`.
4. **`NOTAS.md`** (en `starter/`):
   - (a) En `bitacora.kotlin-library`, cambia `libs.findLibrary("junit-bom").get()` por `libs.junit.bom` y ejecuta `./gradlew -p sandbox build`. Copia el error. ¿Por qué en `build-logic/build.gradle.kts` sí existe `libs.kotlin.gradle.plugin`? Restáuralo.
   - (b) Escribe mal el nombre (`"junit-bomm"`) con `.get()` y copia el mensaje. Cámbialo por `.orElseThrow { GradleException("...") }` con tu mensaje y vuelve a ejecutar. ¿Cuál dejarías y por qué? ¿En qué momento salta el error: al compilar `build-logic` o al configurar `core`?
   - (c) Deja vacío el `build.gradle.kts` raíz y ejecuta `./gradlew -p sandbox build`: ¿qué avisa Gradle? Después escribe solo `alias(libs.plugins.kotlin.jvm) apply false`: ¿sigue el aviso? ¿Por qué? Restáuralo.
   - (d) Para añadir un cuarto módulo de tipo biblioteca (por ejemplo, `report`), ¿cuántas líneas tendría su `build.gradle.kts`? ¿Y en la semana 10?
5. **Ejecuta** `./gradlew test` (7 de 7).

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'libs'` en un plugin | Dentro de un convention plugin no hay `libs`: `val libs = versionCatalogs.named("libs")` |
| `No value present` | Un nombre de `findLibrary` / `findBundle` no está en el catálogo; van con guiones: `"junit-bom"` |
| `Unresolved reference 'mainClass'` en `app` | `bitacora.kotlin-application` no aplica `application`: sin el plugin no hay bloque `application { }` |
| `The Kotlin Gradle plugin was loaded multiple times` | El raíz perdió `alias(libs.plugins.kotlin.jvm) apply false` |
| El test de `core` dice que tiene más líneas | `core` es solo el bloque `plugins` con un id; los comentarios no cuentan |

## Entrega

Los 7 tests en verde y `NOTAS.md` con las respuestas del paso 4.

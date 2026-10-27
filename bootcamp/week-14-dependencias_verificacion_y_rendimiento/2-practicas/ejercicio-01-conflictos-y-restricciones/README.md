# Ejercicio 01 — Conflictos y restricciones

⏱️ 2 h · Teoría previa: 01 y 02

## Objetivo

Entender por qué `kotlin-stdlib` y `org.jetbrains:annotations` acaban en la versión que tienen. Después, subir `annotations` a la 26.1.0 con una restricción documentada, con la versión en el catálogo, y quitarla del classpath de ejecución.

## Cómo está montado

`starter/sandbox/` es un programa pequeño que usa coroutines y serialization, con sus BOMs en el catálogo (`sandbox/gradle/libs.versions.toml`). Los tests (`src/test/kotlin/BuildTest.kt`, no los toques) copian `sandbox/` a una carpeta temporal con la clase `Sandbox` (ya hecha), que también sabe pedir el árbol de `dependencies` y la salida de `dependencyInsight`. Dos tests pasan desde el principio: el conflicto de `kotlin-stdlib` ya se resuelve hacia la versión más alta, y el programa funciona. Tienen que seguir pasando. Para probarlo a mano: `./gradlew -p sandbox dependencies --configuration runtimeClasspath`.

## Pasos

1. **Mira antes de tocar**: ejecuta `dependencies` sobre `runtimeClasspath` y `dependencyInsight --dependency annotations`. Apunta en `NOTAS.md` quién pide cada versión de `annotations` y cuál gana.
2. **El catálogo** (`sandbox/gradle/libs.versions.toml`): la versión `jetbrains-annotations = "26.1.0"` y la biblioteca `org.jetbrains:annotations` con esa versión.
3. **La restricción** (`sandbox/build.gradle.kts`): `constraints { implementation(libs.jetbrains.annotations) { because("...") } }`. El texto de `because` explica la decisión con tus palabras.
4. **La exclusión**: `configurations.runtimeClasspath { exclude(group = "org.jetbrains", module = "annotations") }`.
5. **Ejecuta** `./gradlew test` (7 de 7).
6. **`NOTAS.md`** (en `starter/`):
   - (a) En `dependencyInsight --dependency kotlin-stdlib`, ¿qué tres versiones se piden y quién pide cada una? ¿Por qué es razonable que gane la 2.4.21, y cuándo no lo sería?
   - (b) Añade `configurations.all { resolutionStrategy.failOnVersionConflict() }` y ejecuta `./gradlew -p sandbox compileKotlin`. ¿Qué conflictos aparecen? Quítalo.
   - (c) Quita el bloque `because` y mira `dependencyInsight --dependency annotations --configuration compileClasspath`. ¿Qué se pierde? Restáuralo.
   - (d) Cambia `configurations.runtimeClasspath` por `configurations.all` en la exclusión y ejecuta `./gradlew -p sandbox run`. ¿Qué pasa y por qué? Restáuralo.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| `Unresolved reference 'jetbrains'` en `libs.jetbrains.annotations` | El nombre de la biblioteca en el catálogo no es `jetbrains-annotations` |
| `theVersionLivesInTheCatalog` falla | La versión está escrita en `build.gradle.kts` (`"org.jetbrains:annotations:26.1.0"`): usa `libs.jetbrains.annotations` |
| `theConstraintSaysWhy` falla | Falta `because("...")`, o va fuera del bloque de la restricción |
| `aConstraintRaisesAnnotations` falla y no hay `(c)` en el árbol | La pusiste como dependencia normal (`implementation(...)` fuera de `constraints { }`) |
| `annotationsIsNotShippedToRun` falla | La exclusión no está en `runtimeClasspath`, o el grupo o el módulo están mal escritos |
| `e: java.lang.RuntimeException: Exception while generating code for public constructor` | Excluiste `annotations` también al compilar: paso 6d |

## Entrega

Los 7 tests en verde y `NOTAS.md` con lo del paso 1 y las respuestas del paso 6.

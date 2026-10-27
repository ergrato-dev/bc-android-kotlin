# Proyecto — La Bitácora con build-logic

⏱️ 4 h · Capa 10 del [proyecto hilo conductor](../../../docs/proyecto-hilo-conductor.md).

## Qué construyes

Tu Bitácora de tres módulos deja de repetir configuración: un build incluido, **`build-logic`**, con tres **convention plugins** (`bitacora.kotlin-base`, `bitacora.kotlin-library` y `bitacora.kotlin-application`), y cada módulo con una línea que dice qué es y lo que tiene de propio. Es la estructura en la que entrarán, en la fase 3, los convention plugins de Android.

## Dónde trabajas

En **tu repositorio**, rama `week-11`, a partir de lo que entregaste en la semana 10. El `starter/` trae el esqueleto: el raíz que ejecuta los tests de TestKit (`src/test`, ya hecho), los tres módulos como quedaron en la semana 10, `build-logic/settings.gradle.kts` hecho y el resto de `build-logic` con los pasos en comentarios. Copia a tu repositorio la carpeta `build-logic/` y los archivos de `src/test` que no tengas; tu código de `core`, `data` y `app` no cambia.

## Pasos

1. **`settings.gradle.kts`**: `pluginManagement { includeBuild("build-logic") }` al principio.
2. **Catálogo y `build-logic/build.gradle.kts`**: la biblioteca `kotlin-gradle-plugin` (`version.ref = "kotlin"`), `kotlin-dsl` e `implementation(libs.kotlin.gradle.plugin)`.
3. **`bitacora.kotlin-base`**: el plugin de Kotlin, `jvmToolchain(21)`, `allWarningsAsErrors` y `useJUnitPlatform()`.
4. **`bitacora.kotlin-library`**: la base, `java-library` y las dependencias de prueba sacadas del catálogo con `versionCatalogs.named("libs")`. Si una entrada no está, el error tiene que decirlo **con tus palabras** (`orElseThrow`).
5. **`bitacora.kotlin-application`**: la base y `application`.
6. **Módulos**: cada uno aplica **un** convention plugin y solo conserva lo suyo. El raíz sigue declarando `kotlin-jvm` y `kotlin-serialization` (el banco de pruebas aplica `kotlin-jvm`; los dos, para que los módulos los encuentren cargados).
7. **Escribe al menos 6 tests más** en `BuildTest.kt`, con el valor esperado escrito **a mano**:
   - `:app:run` produce la salida esperada de tu dominio,
   - las clases de `core` y de `app` están compiladas para Java 21 (bytes 6 y 7 del `.class`),
   - un warning en cualquier módulo hace fallar el build,
   - un cambio en un convention plugin reconstruye `build-logic` y deja `UP_TO_DATE` los `compileKotlin` de los módulos,
   - ningún módulo repite `jvmToolchain`, `allWarningsAsErrors` ni las dependencias de prueba,
   - `bitacora.kotlin-library` trae JUnit 6.1.3 al `testRuntimeClasspath` de `data`,
   - el plugin de Kotlin se carga una vez (ningún `loaded multiple times`),
   - (opcional) un cuarto módulo de tipo biblioteca, creado en el test con tres líneas de `build.gradle.kts` y un test propio, compila y pasa su test.
8. **Explica en tu README** qué hace cada convention plugin, qué se quedó en cada módulo y por qué, y por qué el raíz conserva `apply false`.
9. **Abre el pull request** `week-11` → `main` con la salida de `./gradlew test` y la de `./gradlew :app:run`.

## Criterios

Los de la [rúbrica](../rubrica-evaluacion.md): sin warnings, una base y un plugin por tipo de módulo, ninguna versión en `build-logic`, módulos sin configuración repetida, raíz con `apply false`, tests con valores calculados a mano.

## Pistas

- `./gradlew -p build-logic build` compila los convention plugins solos: los errores de un plugin salen sin el ruido de los módulos.
- En un test, `project.file("build-logic/src/main/kotlin/bitacora.kotlin-base.gradle.kts").appendText("\ndescription = \"x\"\n")` cambia un plugin de la copia sin tocar el tuyo. Un comentario no sirve: no cambia el jar.
- Para un warning, un uso de algo `@Deprecated` basta: `"@Deprecated(\"x\")\nfun old() = 1\n\nfun useOld() = old()\n"`.
- `result.outcome(":build-logic:jar")` funciona: las tareas del build incluido salen en el resultado con su nombre delante.
- Para el cuarto módulo, añade `include(":report")` al `settings.gradle.kts` de la copia.
- El raíz no aplica tus convention plugins a propósito: si lo hiciera, Kotlin volvería a cargarse junto con `build-logic` (teoría 04).

# Diseñar y probar build-logic

> Un convention plugin es código que se ejecuta en cada build, así que se prueba como el resto: con tests de TestKit que arrancan Gradle sobre una copia del proyecto y comprueban **lo que el plugin provoca**, no cómo está escrito.

## 🎯 Objetivos

- Comprobar con TestKit el efecto de un convention plugin en los módulos
- Aplicar una lista de comprobación a `build-logic`
- Reconocer los errores frecuentes y su causa

## 1. Lo que se prueba

| Quieres saber | Cómo lo compruebas |
| --- | --- |
| El plugin sale de `build-logic` | `:build-logic:compileKotlin` está en `result.tasks` |
| El programa sigue funcionando | `:app:run -q` y comparas con la salida **escrita a mano** |
| El JDK es el de la base | Lees los bytes 6 y 7 de un `.class`: 65 es Java 21 |
| Los warnings son errores en todos | Añades un uso de algo `@Deprecated` en `app` y esperas `warnings found and -Werror specified` |
| Los tests se ejecutan | Existe `core/build/test-results/test/TEST-CoreTest.xml` |
| La biblioteca trae las pruebas | `:data:dependencies --configuration testRuntimeClasspath` contiene `org.junit:junit-bom:6.1.3` |
| Un programa no es una biblioteca | `:core:run` falla con `Cannot locate tasks that match ':core:run'` |
| Cambiar un plugin no recompila | Añades una línea a un convention plugin: `:build-logic:jar` `SUCCESS` y cada `compileKotlin` `UP_TO_DATE` |
| Kotlin se carga una vez | La salida de `build` no contiene `loaded multiple times` |
| Ninguna versión fuera del catálogo | Ningún archivo de `build-logic` contiene `\d+\.\d+\.\d+` |

El test del JDK lee el formato de clase sin herramientas:

```kotlin
val bytes = project.file("core/build/classes/kotlin/main/Level.class").readBytes()
assertEquals(65, (bytes[6].toInt() shl 8) or bytes[7].toInt()) // 65 = Java 21
```

> 🔬 Medido: con `jvmToolchain(21)` en la base, `Level.class` empieza por `CA FE BA BE 00 00 00 41`; `0x41` es 65.

## 2. Ejecutar build-logic solo

Como es un build normal, se puede trabajar en él sin el principal:

```bash
./gradlew -p build-logic build     # compila los convention plugins
./gradlew -p build-logic tasks     # sus tareas
```

Útil cuando un plugin no compila: el error sale sin el ruido de los módulos.

## 3. Lista de comprobación

- [ ] `settings.gradle.kts` incluye `build-logic` en `pluginManagement`
- [ ] `build-logic/settings.gradle.kts` declara sus repositorios e importa `../gradle/libs.versions.toml`
- [ ] Ninguna versión escrita en `build-logic`: el plugin de Kotlin entra como `libs.kotlin.gradle.plugin`
- [ ] Una base con lo común y un plugin por tipo de módulo; ningún `if` sobre el nombre del módulo
- [ ] Cada módulo aplica **un** convention plugin y solo añade lo suyo
- [ ] El raíz declara `kotlin-jvm` (y los demás plugins de terceros) con `apply false`
- [ ] `./gradlew build` pasa sin avisos; repetido, todo `UP-TO-DATE`, también `:build-logic:`
- [ ] Tests del build que comprueban el efecto de los plugins

## 4. Errores frecuentes

| Síntoma | Causa |
| --- | --- |
| `Plugin [id: 'bitacora.x'] was not found ... Included Builds (No included builds contain this plugin)` | El nombre del archivo no coincide con el id, o falta `includeBuild("build-logic")` |
| `Unresolved reference 'jvmToolchain'` (y `None of the following candidates is applicable` en `kotlin {`) en un convention plugin | Falta `id("org.jetbrains.kotlin.jvm")` en su `plugins { }`: sin el plugin no hay extensión `kotlin { }` |
| `Unresolved reference 'libs'` en un convention plugin | Dentro de un plugin no hay `libs`: `versionCatalogs.named("libs")` |
| `No value present` al aplicar un plugin | Un `findLibrary` / `findBundle` con un nombre que no está en el catálogo; usa `orElseThrow` con tu mensaje |
| `The Kotlin Gradle plugin was loaded multiple times` | El raíz no declara `kotlin-jvm` con `apply false` |
| Un cambio en `build-logic` recompila todos los módulos | Lo mismo: Kotlin se está cargando junto con `build-logic` |

## ✅ Comprueba

- [ ] ¿Cómo compruebas con un test que un convention plugin fija el JDK 21?
- [ ] ¿Por qué un test que solo busca `id("bitacora.kotlin-base")` en un archivo no basta?
- [ ] ¿Qué comando compila los convention plugins sin configurar los módulos?

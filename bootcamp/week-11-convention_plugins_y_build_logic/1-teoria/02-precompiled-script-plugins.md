# Precompiled script plugins

> La forma más directa de escribir un convention plugin es la que ya sabes escribir: un archivo `.gradle.kts`. El plugin `kotlin-dsl` lo compila y lo convierte en un plugin con id propio. Se llama *precompiled script plugin*.

## 🎯 Objetivos

- Montar el build `build-logic` con el plugin `kotlin-dsl`
- Escribir un convention plugin como archivo `.gradle.kts` y saber qué id recibe
- Explicar por qué `build-logic` depende del plugin de Kotlin como biblioteca

## 1. El build de los plugins

```kotlin
// build-logic/settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "build-logic"
```

```kotlin
// build-logic/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21") // la lección 3 lo pasa al catálogo
}
```

- **`kotlin-dsl`** viene con Gradle: no se le escribe versión. Compila los `.gradle.kts` de `src/main/kotlin` y genera un plugin por archivo.
- **Repositorios propios**: `build-logic` es otro build; el `FAIL_ON_PROJECT_REPOS` del principal no le llega. Hay dos fuentes: el **Gradle Plugin Portal**, de donde sale `kotlin-dsl` (es el repositorio de plugins por defecto, en `pluginManagement`, y no hay que escribirlo), y **Maven Central**, para el plugin de Kotlin. Las dos sirven código que se ejecuta en tu build: se revisan igual que las del principal.

> 🔬 Medido: el Plugin Portal también sirve el plugin de Kotlin (replica Maven Central). Con `gradlePluginPortal()` delante de `mavenCentral()` en `repositories`, `kotlin-gradle-plugin` se descarga de `plugins.gradle.org`; con solo `mavenCentral()`, de `repo.maven.apache.org`, y el build pasa igual. Declara solo lo que necesitas: cada repositorio de más es una fuente más en la que confías.

> 🔬 Medido en Gradle 9.8.2: `kotlin-dsl` se resuelve como `org.gradle.kotlin.kotlin-dsl` 6.7.6 y compila los scripts con el Kotlin que trae Gradle (2.4.10). Los módulos siguen compilando con Kotlin 2.4.21: `build-logic` **depende** del plugin 2.4.21, no lo aplica a sí mismo, y no hay aviso de versiones.

## 2. Un archivo, un plugin

```kotlin
// build-logic/src/main/kotlin/bitacora.kotlin-base.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}
```

- **El id es el nombre del archivo** sin `.gradle.kts`: `bitacora.kotlin-base`. El prefijo `bitacora.` evita chocar con plugins publicados.
- **El cuerpo es un `build.gradle.kts` normal**: lo que escribías en cada módulo, ahora una vez.
- **`id("org.jetbrains.kotlin.jvm")` sin versión.** Dentro de un precompiled script plugin no se escriben versiones en `plugins { }`: el plugin tiene que estar ya en el classpath de `build-logic`. Por eso `build-logic` declara `kotlin-gradle-plugin` como `implementation`: es **el mismo plugin**, pedido como biblioteca (`grupo:artefacto:versión`) en lugar de como id.
- **`kotlin { }` y `tasks.test` funcionan** porque `kotlin-dsl` genera los accesos tipados de los plugins del bloque `plugins { }` del archivo, igual que en un `build.gradle.kts`.

## 3. Aplicarlo

```kotlin
// core/build.gradle.kts
plugins {
    id("bitacora.kotlin-base")
    `java-library`
}
```

Sin versión: el plugin sale de un build incluido. La primera vez, la salida muestra cómo se construye:

```
> Task :build-logic:generatePrecompiledScriptPluginAccessors
> Task :build-logic:compileKotlin
> Task :build-logic:pluginDescriptors
> Task :build-logic:jar
> Task :core:compileKotlin
...
```

`generatePrecompiledScriptPluginAccessors` es el paso de los accesos tipados; `pluginDescriptors` escribe qué id corresponde a qué clase. La segunda vez todas quedan `UP-TO-DATE`.

## 4. Si el id no existe

```
Plugin [id: 'bitacora.kotlin-libary'] was not found in any of the following sources:

- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Included Builds (No included builds contain this plugin)
- Plugin Repositories (plugin dependency must include a version number for this source)
```

> 🔬 Medido: es el mismo mensaje con un id mal escrito que sin `includeBuild("build-logic")` en `settings`. Las tres líneas son los tres sitios donde Gradle busca; la segunda es la tuya. Revisa primero el nombre del archivo y después `settings`.

## ✅ Comprueba

- [ ] ¿Qué id tiene el plugin `build-logic/src/main/kotlin/bitacora.kotlin-library.gradle.kts`?
- [ ] ¿Por qué `build-logic` declara `kotlin-gradle-plugin` como `implementation`?
- [ ] ¿De qué repositorio sale `kotlin-dsl` y de cuál el plugin de Kotlin?

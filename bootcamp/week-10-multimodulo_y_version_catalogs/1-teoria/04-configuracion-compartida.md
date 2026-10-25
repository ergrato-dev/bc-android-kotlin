# Configuración compartida

> Después del catálogo siguen repetidos los repositorios, el `kotlin { }` y el `tasks.test`. Esta lección arregla los repositorios y deja claro **qué sobra y por qué** la semana que viene hay una solución mejor.

## 🎯 Objetivos

- Centralizar los repositorios en `settings.gradle.kts`
- Hacer que Gradle rechace los repositorios sueltos en los módulos
- Reconocer la configuración repetida y las opciones para compartirla

## 1. Repositorios en un solo sitio

En lugar de `repositories { mavenCentral() }` en cada módulo:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}
```

`FAIL_ON_PROJECT_REPOS` hace que cualquier módulo que declare **sus propios** repositorios **falle**:

```
Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file 'app/build.gradle.kts'
```

> 🔬 Medido: con el bloque en `settings` y `repositories { mavenCentral() }` todavía en `app`, el build falla con ese mensaje; al borrarlos de los tres módulos, `build` pasa.

Por qué importa: de dónde se descargan las dependencias es una decisión de **seguridad** (a quién confías el código que ejecutas). Si cada módulo puede añadir un repositorio, cualquiera podría meter uno sin que se note. Un sitio, una revisión.

## 2. Lo que sigue repetido

Después de la lección 3 y de los repositorios, cada módulo conserva:

```kotlin
kotlin {
    jvmToolchain(21)
    compilerOptions { allWarningsAsErrors.set(true) }
}

tasks.test { useJUnitPlatform() }
```

y las dependencias de prueba. Es poco con tres módulos, pero cada módulo nuevo lo copia, y cada cambio (JDK, opciones del compilador) se hace N veces.

## 3. Las opciones

| Opción | Qué es | Pega |
| --- | --- | --- |
| Copiar y pegar | Lo que hay ahora | Se desincroniza |
| `subprojects { ... }` o `allprojects { ... }` en el raíz | El raíz configura a todos los módulos | Los módulos dependen de un archivo que no ven; cuesta ver qué aplica a quién, y mezcla la configuración de todos en un solo sitio |
| **Convention plugins** | Un plugin propio, con nombre, que contiene la configuración común; cada módulo escribe `plugins { id("bitacora.kotlin-library") }` | Hay que crearlos (es el tema de la semana 11) |

Una sola línea en cada módulo, que se puede **leer, probar y versionar**: esa es la solución buena. Esta semana dejas la duplicación **a propósito**, y la mides: así sabrás qué problema resuelve la siguiente.

## 4. Qué compartir y qué no

| Comparte | No compartas |
| --- | --- |
| Versiones (catálogo) | Dependencias que solo usa un módulo |
| Repositorios (`settings`) | El `application { mainClass }` de `app` |
| Opciones del compilador, JDK, JUnit (convention plugin) | Un `api` o `implementation` concretos |

Regla: se comparte lo que **es igual por definición** (la versión de Kotlin, el JDK). Lo que depende del módulo se queda en el módulo.

## ✅ Comprueba

- [ ] ¿Qué hace `FAIL_ON_PROJECT_REPOS` y por qué es una medida de seguridad?
- [ ] ¿Qué configuración sigue repetida después del catálogo?
- [ ] ¿Qué desventaja tiene `subprojects { }` frente a un plugin?

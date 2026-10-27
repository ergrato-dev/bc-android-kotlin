# De script a clase

> Un convention plugin aplica decisiones fijas. Cuando el plugin necesita **parámetros**, **lógica** o **pruebas propias**, o lo van a usar otros equipos, se escribe como una clase: un plugin **binario**. Es lo mismo que hacen `java-library` o el plugin de Kotlin.

## 🎯 Objetivos

- Escribir un plugin como clase que implementa `Plugin<Project>`
- Registrarlo con un id en `gradlePlugin { }`
- Decidir entre un precompiled script plugin y un plugin binario

## 1. La clase

```kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildInfoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // configura el proyecto: extensiones, tareas, reacciones a otros plugins
    }
}
```

Un plugin es eso: una función `apply` que recibe el proyecto. Gradle la llama **una vez por proyecto** que aplica el plugin, en la fase de configuración.

> Un precompiled script plugin (semana 11) es lo mismo por dentro: `kotlin-dsl` convierte el `.gradle.kts` en una clase con su `apply`. La diferencia está en cómo lo escribes, no en cómo funciona.

## 2. El build del plugin

```kotlin
// build.gradle.kts del plugin
plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("buildInfo") {
            id = "bitacora.build-info"
            implementationClass = "BuildInfoPlugin"
        }
    }
}
```

- **`kotlin-dsl`** trae Kotlin, la API de Gradle y `java-gradle-plugin`, que es el que da el bloque `gradlePlugin { }`.
- **`register("buildInfo")`**: un nombre interno. Lo que usan los demás es el **`id`**.
- **`implementationClass`**: el nombre completo de la clase (con su paquete, si lo tiene).

Con esto, el jar lleva un archivo `META-INF/gradle-plugins/bitacora.build-info.properties` con una línea, `implementation-class=BuildInfoPlugin`. Así encuentra Gradle la clase a partir del id.

> 🔬 Medido en Gradle 9.8.2: con `kotlin-dsl`, `kotlin("test")` resuelve a `kotlin-test:2.4.10`, el Kotlin que trae Gradle. Es lo esperado: un plugin se ejecuta dentro de Gradle y compila con su Kotlin.

## 3. Cuándo una clase y cuándo un script

| Precompiled script plugin | Plugin binario |
| --- | --- |
| Decisiones fijas del equipo: JDK, opciones, `java-library` | Algo que se **configura**: `buildInfo { domain = "agro" }` |
| Se lee como un `build.gradle.kts` | Lógica con ramas, tipos y tareas propias |
| Vive en `build-logic` y solo lo usa tu proyecto | Se prueba por partes, se publica, lo usan otros |

Los dos conviven. Un convention plugin puede aplicar un plugin binario: `bitacora.kotlin-application` aplicará `bitacora.build-info` en el proyecto de esta semana.

## 4. Dentro del mismo build-logic

Si la clase vive en el mismo `build-logic` que el convention plugin que la aplica, **no** se puede pedir en `plugins { }`:

```kotlin
// bitacora.kotlin-application.gradle.kts
plugins {
    id("bitacora.kotlin-base")
    application
    id("bitacora.build-info")          // no compila
}
```

```
> Task :build-logic:generatePrecompiledScriptPluginAccessors FAILED
Plugin [id: 'bitacora.build-info'] was not found in any of the following sources:
```

> 🔬 Medido: `kotlin-dsl` necesita aplicar los plugins del bloque `plugins { }` **antes** de compilar el `build-logic` (para generar los accesos), y la clase todavía no está compilada. La solución es aplicarla por su clase, fuera del bloque:

```kotlin
plugins {
    id("bitacora.kotlin-base")
    application
}

pluginManager.apply(BuildInfoPlugin::class.java)
```

Los módulos que aplican `bitacora.kotlin-application` reciben la extensión `buildInfo { }` igual: sus accesos se generan después de su propio bloque `plugins { }`, cuando el plugin ya está aplicado.

## ✅ Comprueba

- [ ] ¿Qué función tiene que implementar un plugin binario?
- [ ] ¿Qué escribe `gradlePlugin { }` en el jar y para qué sirve?
- [ ] ¿Por qué un convention plugin no puede pedir en `plugins { }` una clase del mismo `build-logic`?

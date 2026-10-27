# El catálogo dentro de build-logic

> `build-logic` es otro build, así que no ve el `libs` del principal. Sin cuidado, la versión de Kotlin vuelve a estar en dos sitios. Esta lección conecta los dos builds con **un solo catálogo** y enseña cómo se lee desde un convention plugin.

## 🎯 Objetivos

- Compartir `gradle/libs.versions.toml` entre el build principal y `build-logic`
- Usar el catálogo en `build-logic/build.gradle.kts` con `libs.…`
- Leer el catálogo desde un precompiled script plugin con `versionCatalogs`

## 1. Un catálogo, dos builds

Gradle solo carga automáticamente `gradle/libs.versions.toml` **del propio build**. `build-logic` lo importa a mano:

```kotlin
// build-logic/settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

Y el catálogo gana una entrada, el plugin de Kotlin **como biblioteca**:

```toml
[libraries]
kotlin-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
```

Comparte `version.ref = "kotlin"` con `kotlin-test` y con el plugin `kotlin-jvm`: subir Kotlin sigue siendo cambiar **una** línea.

## 2. En el `build.gradle.kts` de build-logic

Aquí `libs` existe, porque `build-logic` tiene su catálogo:

```kotlin
// build-logic/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}
```

## 3. Dentro de un convention plugin

Dentro de `bitacora.kotlin-library.gradle.kts`, `libs.junit.bom` **no compila**:

```
e: .../bitacora.kotlin-library.gradle.kts:8:33 Unresolved reference 'libs'.
Execution failed for task ':build-logic:compileKotlin'
```

> 🔬 Medido en Gradle 9.8.2. El motivo: un precompiled script plugin se compila **una vez**, sin saber qué build lo aplicará. El acceso `libs` lo genera Gradle para los scripts de cada build, según su catálogo; el plugin no puede contar con él.

Se pide el catálogo **por nombre**, al aplicarse el plugin:

```kotlin
// build-logic/src/main/kotlin/bitacora.kotlin-library.gradle.kts
plugins {
    id("bitacora.kotlin-base")
    `java-library`
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findBundle("testing").get())
}
```

| Método | Devuelve | Ejemplo |
| --- | --- | --- |
| `findLibrary("x")` | La biblioteca `x` de `[libraries]` | `findLibrary("junit-bom")` |
| `findBundle("x")` | El *bundle* `x` | `findBundle("testing")` |
| `findVersion("x")` | La versión `x` de `[versions]` | `findVersion("kotlin")` |
| `findPlugin("x")` | El plugin `x` de `[plugins]` | `findPlugin("kotlin-jvm")` |

Los nombres van **con guiones**, como en el TOML (`"junit-bom"`), no con puntos. Cada método devuelve un `Optional`: vacío si el nombre no existe.

## 4. Cuando el nombre no existe

Con `findLibrary("junit-bomm").get()`, el build falla así:

```
An exception occurred applying plugin request [id: 'bitacora.kotlin-library']
> Failed to apply plugin 'bitacora.kotlin-library'.
   > No value present
```

`No value present` es el mensaje de `Optional.get()`: dice que algo faltó, no qué. Con `orElseThrow` eliges el mensaje:

```kotlin
testImplementation(platform(libs.findLibrary("junit-bom").orElseThrow { GradleException("El catálogo no tiene la biblioteca junit-bom") }))
```

```
> Failed to apply plugin 'bitacora.kotlin-library'.
   > El catálogo no tiene la biblioteca junit-bom
```

> 🔬 Medido: los dos mensajes de arriba son los de Gradle 9.8.2. El error salta **al configurar** el módulo que aplica el plugin, no al compilar `build-logic`: el compilador no sabe qué hay en el catálogo.

## 5. Qué queda en cada sitio

| Sitio | Qué guarda |
| --- | --- |
| `gradle/libs.versions.toml` | Todas las versiones, también la del plugin de Kotlin que usa `build-logic` |
| `build-logic/settings.gradle.kts` | De dónde se descargan los plugins y qué catálogo se lee |
| Convention plugins | Decisiones (JDK, opciones, qué bibliotecas de prueba), sin ninguna versión escrita |

## ✅ Comprueba

- [ ] ¿Por qué `build-logic` no ve el catálogo del build principal sin importarlo?
- [ ] ¿Por qué `libs.junit.bom` funciona en `build-logic/build.gradle.kts` y no dentro de un convention plugin?
- [ ] ¿Qué devuelve `findLibrary` y qué pasa si el nombre no existe?

# Restricciones y exclusiones

> La versión más alta gana, pero a veces la más alta que alguien pide no es la que tú quieres: una transitiva tiene un fallo o un aviso de seguridad, y la biblioteca que la trae todavía no la ha subido. Y a veces una transitiva sobra donde llega.

## 🎯 Objetivos

- Subir una dependencia transitiva con una restricción, y dejar escrito por qué
- Poner la versión de la restricción en el catálogo
- Quitar una transitiva de una sola configuración con `exclude`

## 1. Restricciones: `constraints { }`

`kotlinx-coroutines-core` 1.11.0 trae `org.jetbrains:annotations` 23.0.0. Si el equipo decide usar la 26.1.0, no hace falta esperar a una versión nueva de coroutines:

```kotlin
dependencies {
    constraints {
        implementation(libs.jetbrains.annotations) {
            because("el equipo usa en todos los módulos la versión del catálogo")
        }
    }
}
```

```
|    \--- org.jetbrains:annotations:13.0 -> 26.1.0
|         +--- org.jetbrains:annotations:23.0.0 -> 26.1.0
\--- org.jetbrains:annotations:26.1.0 (c)
```

```
   Selection reasons:
      - By constraint: el equipo usa en todos los módulos la versión del catálogo
      - By conflict resolution: between versions 26.1.0, 13.0 and 23.0.0
```

> 🔬 Medido en Gradle 9.8.2: así sale en `dependencies` y en `dependencyInsight`.

- Una restricción **no añade** la dependencia. Solo entra en el conflicto si algún otro trae el módulo. Si mañana nadie trae `annotations`, la restricción no hace nada, y no hay que acordarse de borrarla.
- La restricción participa en el conflicto como una petición más: la 26.1.0 gana porque es la más alta.
- **`because`** es para la persona que lea el build dentro de un año. Sin él, una restricción parece un capricho y alguien la borra. Con un aviso de seguridad, pon su identificador (`because("CVE-2026-XXXXX en la 23.0.0")`).

La versión va en el catálogo, como todas (semana 10):

```toml
[versions]
jetbrains-annotations = "26.1.0"

[libraries]
jetbrains-annotations = { module = "org.jetbrains:annotations", version.ref = "jetbrains-annotations" }
```

Esa versión cumple la regla del curso: completa, publicada hace más de dos semanas (Maven Central, 2026-02-18) y sin avisos en OSV. Una restricción cambia lo que se ejecuta: se revisa igual que una dependencia directa.

## 2. Exclusiones: `exclude`

`org.jetbrains:annotations` solo contiene anotaciones como `@NotNull`, que sirven al compilar y al IDE. En ejecución no hace falta, pero viaja a la distribución de `app`. Se puede quitar de **una** configuración:

```kotlin
configurations.runtimeClasspath {
    exclude(group = "org.jetbrains", module = "annotations")
}
```

> 🔬 Medido: con la exclusión, `runtimeClasspath` ya no tiene `annotations`, `compileClasspath` sigue teniendo la 26.1.0, el programa funciona igual y `installDist` deja de copiar `annotations-26.1.0.jar` a `build/install/bitacora/lib`.

Una exclusión es más peligrosa que una restricción. Quitas algo que otra biblioteca dijo necesitar, y si se equivoca quien excluye, el error sale al ejecutar (`NoClassDefFoundError`). Excluye solo lo que entiendes, en la configuración más concreta posible, y con un test que ejecute el programa.

## 3. Qué usar

| Quieres | Usa |
| --- | --- |
| Una versión concreta de algo que declaras tú | La versión en el catálogo, como siempre |
| Las versiones de una familia de bibliotecas, alineadas | Su BOM: `platform(...)` |
| Subir una transitiva | `constraints { }` con `because` |
| Que una transitiva no llegue a un classpath | `exclude` en esa configuración |
| Ver todos los conflictos | `failOnVersionConflict()`, un rato |

## ✅ Comprueba

- [ ] ¿Qué diferencia hay entre añadir una dependencia y añadir una restricción?
- [ ] ¿Para qué sirve `because` y qué pondrías si la causa es un aviso de seguridad?
- [ ] ¿Por qué `annotations` se excluye de `runtimeClasspath` y no de `compileClasspath`?

# Por qué módulos y cómo se declaran

> Hasta ahora, toda tu Bitácora era un solo proyecto. Un proyecto grande tiene un coste: cualquier cambio recompila todo, y nada impide que la pantalla toque la base de datos directamente. Dividirlo en **módulos** pone fronteras que Gradle hace cumplir.

## 🎯 Objetivos

- Explicar para qué sirve dividir un proyecto en módulos
- Declarar módulos en `settings.gradle.kts` y reconocer su ruta (`:core`)
- Pedir a Gradle una tarea de un solo módulo y leer qué ejecuta

## 1. Qué es un módulo

En Gradle, un **módulo** (o *subproyecto*) es una carpeta con su propio `build.gradle.kts` y su propio código, que forma parte de un build mayor. El **proyecto raíz** los agrupa.

```
bitacora/
├── settings.gradle.kts          # incluye los módulos
├── build.gradle.kts             # el raíz: plugins comunes, sin código propio
├── gradle/libs.versions.toml    # el catálogo de versiones (lección 3)
├── core/                        # modelo, reglas, informes
│   ├── build.gradle.kts
│   └── src/{main,test}/kotlin
├── data/                        # guardar y leer el archivo
│   ├── build.gradle.kts
│   └── src/{main,test}/kotlin
└── app/                         # el programa
    ├── build.gradle.kts
    └── src/main/kotlin
```

Ganas tres cosas:

| Ventaja | Cómo |
| --- | --- |
| **Fronteras** | `core` no puede usar nada de `data` o `app`: no tiene la dependencia, y no compila (lección 2) |
| **Compilación más rápida** | Si cambias `app`, `core` y `data` no se recompilan; ni siquiera todo `core` cuando solo cambia su implementación |
| **Pruebas aisladas** | `./gradlew :core:test` no necesita ni compilar `app` |

Y pagas una: **cada módulo repite configuración** (plugin, `kotlin { }`, pruebas). Las lecciones 3 y 4 la reducen; la semana siguiente la elimina.

## 2. Declararlos

Solo `settings.gradle.kts` decide qué módulos existen:

```kotlin
rootProject.name = "bitacora"

include(":core", ":data", ":app")
```

- `:core` es la **ruta** del proyecto: `:` es la raíz, `:core` una carpeta `core/` dentro. Es lo que escribes delante de una tarea: `./gradlew :core:test`.
- Una carpeta con `build.gradle.kts` que **no** está en `include` no es un módulo: Gradle la ignora.

Comprueba qué ve Gradle:

```bash
./gradlew projects
# Project hierarchy:
# +--- Project ':app'
# +--- Project ':core'
# \--- Project ':data'
```

## 3. Tareas de un módulo

```bash
./gradlew test            # test de TODOS los módulos
./gradlew :core:test      # solo core
./gradlew :data:test      # data, y lo que data necesita de core
./gradlew :app:run        # el programa, y lo que necesita
```

> 🔬 Medido: `./gradlew :data:test --dry-run` lista `:core:compileKotlin`, `:core:jar`, `:data:compileKotlin`, `:data:test`... y **ninguna** tarea de `:app`. Gradle construye lo que necesita, no más.

Y al revés: `./gradlew :app:compileKotlin` ejecuta primero `:core:compileKotlin`, después `:data:compileKotlin` y al final `:app:compileKotlin`: el **orden de dependencia** de los módulos, no el alfabético.

## 4. Cómo dividir (criterios)

- **Por razón de cambio**: lo que cambia por los mismos motivos va junto (las reglas del dominio, por un lado; el formato del archivo, por otro).
- **Dependencias en una dirección**: `app → data → core`. Lo más estable (`core`) no conoce a nadie.
- **Pocos módulos al principio**: tres bien pensados valen más que veinte. Un módulo se crea cuando hay una frontera que defender, no por simetría.

## ✅ Comprueba

- [ ] ¿Qué significa la ruta `:core:test`?
- [ ] ¿Por qué `./gradlew :data:test` no compila `app`?
- [ ] ¿Qué pasa con una carpeta con `build.gradle.kts` que no está en `include`?

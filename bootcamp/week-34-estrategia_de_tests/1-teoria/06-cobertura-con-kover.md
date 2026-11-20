# Cobertura con Kover

> La cobertura dice qué líneas del código ejecutan los tests. No dice si esos tests comprueban algo, pero sí señala lo que nadie ejecuta. [Kover](https://github.com/Kotlin/kotlinx-kover) es la herramienta de cobertura de JetBrains para Kotlin. En la Bitácora junta en un solo informe los cuatro módulos `core` y para el build si la cobertura baja. Lo que encontró al medir está en esta teoría.

## 🎯 Objetivos

- Aplicar Kover a varios módulos y juntar sus informes en el raíz
- Leer el informe: líneas, ramas, y lo que no debería contar
- Hacer que `check` falle si la cobertura baja
- Saber lo que la cobertura no dice

> 🔬 Medido con Kover 0.9.11, Gradle 9.8.2 y AGP 9.4.1.

## 1. Las piezas

Kover es un plugin de Gradle (`org.jetbrains.kotlinx.kover`). En cada módulo donde se aplica:

- **Añade un agente a las tareas `test`.** El agente instrumenta las clases al cargarlas y apunta qué se ejecuta. No hay que tocar los tests ni volver a ejecutarlos para el informe, porque el agente va siempre: con los tests `UP-TO-DATE`, el informe reutiliza lo que se apuntó la última vez.
- **Crea las tareas** `koverHtmlReport`, `koverXmlReport`, `koverLog` (en la consola), `koverVerify` y otras.

En el raíz, `kover(project(...))` junta los módulos en un solo informe:

```kotlin
// build.gradle.kts del raíz
plugins {
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":core:model"))
    kover(project(":core:database"))
    kover(project(":core:network"))
    kover(project(":core:data"))
}
```

En la Bitácora, el plugin entra en los módulos `core` por `bitacora.kotlin-library`, y `build-logic` lo recibe por el catálogo (`kover-gradle-plugin`), igual que AGP. Así, un módulo de Kotlin nuevo tiene cobertura sin tocar su `build.gradle.kts`, aunque para entrar en el informe del raíz hay que añadirlo a la lista.

> 🔬 Medido: Kover 0.9.11 da un aviso de Gradle 9 al aplicarse, `The Configuration.setVisible(boolean) method has been deprecated. This is scheduled to be removed in Gradle 11`, que viene de `kotlinx.kover.gradle.plugin.commons.ConfigurationsKt.asBucket`. Es del plugin y no rompe el build. Antes de subir a Gradle 11, habrá que subir Kover.

## 2. Lo que no debería contar

El primer informe de la Bitácora, sin filtros:

> 🔬 Medido: 430 de 447 líneas (96 %). Entre lo no cubierto, `BitacoraDatabase_Impl` y `ObservationDao_Impl`, el código que genera Room con KSP.

Ese código no lo escribimos nosotros, y probarlo es probar Room. Se quita con un filtro en el raíz:

```kotlin
kover {
    reports {
        filters {
            excludes {
                classes("*_Impl", "*_Impl$*")
            }
        }
    }
}
```

> 🔬 Medido después del filtro: 136 de 139 líneas (97,8 %) y 116 de 143 ramas (81 %).

El otro código que cuenta sin deber hacerlo son los **fakes**. Kover trata `testFixtures` como código del módulo:

> 🔬 Medido en un proyecto aparte: `FakeStore`, de `src/testFixtures`, sale en el informe como una clase más. Un fake usado por muchos tests sube la cobertura sin probar nada de la app.

Por eso `bitacora.kotlin-library` lo quita:

```kotlin
kover {
    currentProject {
        sources {
            excludedSourceSets.add("testFixtures")
        }
    }
}
```

**Los tests del raíz.** El raíz tiene sus propios tests (TestKit). Con el plugin aplicado, `koverXmlReport` los ejecutaría, y tardan 10 minutos. `instrumentation { disabledForTestTasks.add("test") }` saca la tarea `test` del raíz de Kover: medido con `-m`, `koverXmlReport` ejecuta los `test` de los cuatro módulos `core` y no el del raíz.

## 3. Juntar oculta lo que no se prueba

`:core:network` no tenía tests hasta esta semana. En el informe del raíz salía cubierta:

> 🔬 Medido: en el informe del raíz, `NetworkKt` tiene sus 13 líneas cubiertas, por los tests de `:core:data`. En el del módulo solo (`./gradlew :core:network:koverXmlReport`), 0 de 32 líneas. Con los cinco tests de `NetworkErrorTest`, 22 de 32.

El informe junto dice qué ejecuta la suite entera, mientras que el de un módulo dice qué prueban sus propios tests. Si el módulo de la red se usara desde otra app sin `:core:data`, nada lo probaría. Hay que mirar los dos informes.

## 4. Líneas y ramas

| Unidad | Qué cuenta |
| --- | --- |
| Línea (`LINE`) | Una línea con código ejecutable que se ha ejecutado al menos una vez |
| Rama (`BRANCH`) | Cada camino de un `if`, `when`, `?:`, `&&`... |
| Instrucción | Cada instrucción del bytecode |

Las ramas parecen más exigentes, pero en Kotlin cuentan muchas que no se escriben a mano:

> 🔬 Medido: en el informe de `:core:network`, `RemoteObservation` tiene 24 ramas y 20 sin cubrir. Salen de los valores por defecto del constructor (`latitude: Double? = null`) y del serializador que genera `kotlinx.serialization`, no de un `if` nuestro.

Por eso la regla de la Bitácora es de líneas. Las ramas se miran en el informe HTML, sobre el código que sí escribimos.

## 5. Parar el build

```kotlin
kover {
    reports {
        verify {
            rule {
                minBound(90)   // líneas, en porcentaje
            }
        }
    }
}
```

> 🔬 Medido: con 40 líneas sin probar añadidas a `:core:model`, `./gradlew koverVerify` falla con `Rule violated: lines covered percentage is ...`. `TestingTest.lessCoverageStopsTheBuild` lo comprueba.

`koverVerify` entra en `check`, y por tanto en `build`, si el proyecto tiene la tarea `check`. El raíz de la Bitácora la tiene porque aplica el plugin de Kotlin.

> 🔬 Medido en el ejercicio 02: un raíz que solo carga plugins con `apply false` no tiene `check`, y `./gradlew check` no ejecuta su `koverVerify`. Con `base` en `plugins { }`, sí.

**¿Qué número?** El que hay ahora, un poco por debajo. Así la regla impide bajar, pero no obliga a escribir tests para llegar a una cifra. La Bitácora está en el 97,8 % y la regla pide un 90 %. Un 100 % obliga a probar los `toString()` y lleva a tests que no comprueban nada.

## 6. Android

Kover también funciona en módulos de Android, y crea las tareas por variante:

> 🔬 Medido con AGP 9.4.1, aplicándolo a `:feature:map`: `koverXmlReportDebug`, `koverXmlReportRelease`, `koverVerifyDebug`... El informe de debug da 14 de 27 líneas de `MapScreenKt`: Robolectric compone la pantalla, pero el mapa de Google no se pinta en la JVM.

La Bitácora no lo aplica a la app ni al mapa esta semana, por dos motivos: los tests de Compose con Robolectric miden otra cosa (semana 35), y la regla del 90 % en una pantalla obligaría a probar lo que solo se ve en un dispositivo.

## 7. Lo que no dice

- **Que un test compruebe algo.** Un test que llama a `refresh()` sin `assertEquals` cubre las mismas líneas que uno bueno.
- **Que se prueben los casos que importan.** `toNetworkError()` estaba cubierta al 100 % por los tests de `:core:data`, pero ninguno probaba que un `ConnectTimeoutException` (que también es una `IOException`) sea `TIMEOUT` y no `OFFLINE`. Lo prueba `aTimeoutIsATimeoutAndNotOffline`, y ese test no ha subido la cobertura de líneas del raíz.
- **Lo que no hay.** Una línea que debería existir (una validación que falta) no sale en ningún informe.

La cobertura sirve para encontrar código que nadie ejecuta. No sirve para medir la calidad de los tests.

## Para recordar

- Kover pone un agente en las tareas `test`; `kover(project(...))` en el raíz junta los informes.
- Fuera lo generado (`*_Impl` de Room) y los `testFixtures`: no son código nuestro de la app.
- El informe junto puede ocultar un módulo sin tests propios: mira también el del módulo.
- Regla de líneas, un poco por debajo de lo que hay. `koverVerify` entra en `check` si el proyecto tiene `check`.
- Cubierto no es probado: la cobertura señala lo que no se ejecuta, no si se comprueba algo.

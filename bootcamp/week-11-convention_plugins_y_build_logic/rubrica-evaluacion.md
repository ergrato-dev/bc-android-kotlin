# Rúbrica de Evaluación — Semana 11: Convention plugins y build-logic

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora con build-logic |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es un convention plugin y en qué se diferencia de `subprojects { }`? | 10 |
| 2 | Compara `apply(from = ...)`, `buildSrc` y un build incluido (`build-logic`) | 10 |
| 3 | ¿Cómo se incluye `build-logic` y por qué no aparece en `./gradlew projects`? | 10 |
| 4 | ¿Qué hace el plugin `kotlin-dsl` y qué id recibe `bitacora.kotlin-library.gradle.kts`? | 10 |
| 5 | ¿Por qué `build-logic` declara `kotlin-gradle-plugin` como `implementation` y el convention plugin pide `id("org.jetbrains.kotlin.jvm")` sin versión? | 10 |
| 6 | ¿De qué repositorio sale `kotlin-dsl` y de cuál el plugin de Kotlin? ¿Por qué no conviene declarar repositorios de más? | 10 |
| 7 | ¿Cómo lee `build-logic` el catálogo y por qué no existe `libs` dentro de un convention plugin? | 10 |
| 8 | ¿Qué devuelve `findLibrary`, qué significa `No value present` y cómo se mejora el mensaje? | 10 |
| 9 | ¿Qué va en un convention plugin y qué se queda en el módulo? Da un ejemplo de cada | 10 |
| 10 | ¿Por qué el raíz sigue declarando `kotlin-jvm` con `apply false`? ¿Qué pasa sin esa línea? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Un plugin propio cuya única misión es aplicar las convenciones del equipo (JDK, opciones, pruebas). Con `subprojects { }` el raíz empuja configuración a todos los módulos desde un archivo que ellos no ven; con un convention plugin cada módulo **pide** la suya con una línea visible en su propio archivo, y se puede probar y versionar.
2. `apply(from = ...)` aplica un script suelto: sin `plugins { }` ni accesos tipados. `buildSrc` es una carpeta especial que Gradle compila siempre, de forma implícita. `build-logic` es un build normal, incluido de forma explícita en `settings`, con su propio `settings.gradle.kts`, que se puede construir y probar solo.
3. Con `pluginManagement { includeBuild("build-logic") }` en `settings.gradle.kts`. No es un módulo del build principal (no está en `include`): es otro build que aporta plugins.
4. Compila cada `.gradle.kts` de `src/main/kotlin` como un plugin y genera sus accesos tipados (`kotlin { }`, `tasks.test`). El id es el nombre del archivo sin `.gradle.kts`: `bitacora.kotlin-library`.
5. Dentro de un precompiled script plugin, `plugins { }` no admite versión: el plugin tiene que estar ya en el classpath de `build-logic`. `kotlin-gradle-plugin` es ese mismo plugin pedido como biblioteca (`grupo:artefacto`); su versión sale del catálogo.
6. `kotlin-dsl`, del Gradle Plugin Portal (el repositorio de plugins por defecto); el plugin de Kotlin, de Maven Central. Cada repositorio es una fuente de código que se ejecuta en el build y en la que confías; el Portal también sirve el plugin de Kotlin, así que con él delante se descargaría de ahí.
7. Con `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }` en `build-logic/settings.gradle.kts`. El acceso `libs` lo genera Gradle para los scripts de cada build; el convention plugin se compila una vez sin saber qué build lo aplicará, así que pide el catálogo por nombre: `versionCatalogs.named("libs")`.
8. Un `Optional` con la biblioteca, vacío si el nombre no está. `No value present` es el mensaje de `Optional.get()` sobre uno vacío: no dice qué faltó. Se mejora con `orElseThrow { GradleException("...") }`.
9. En el plugin, lo que es igual por definición en todos los módulos de un tipo: JDK 21, `allWarningsAsErrors`, JUnit, `java-library`. En el módulo, lo propio: `api(project(":core"))`, serialization en `data`, `mainClass` en `app`.
10. Para que el plugin de Kotlin se cargue una vez, desde el raíz, y los módulos lo encuentren ahí. Sin esa línea, o avisa `The Kotlin Gradle plugin was loaded multiple times` (si un módulo pide otro plugin de Kotlin con versión), o Kotlin se carga junto con `build-logic` y cualquier cambio en un convention plugin cambia el classpath de `compileKotlin`: `Class path of task ':core:compileKotlin' has changed` y se recompilan todos los módulos.

**Ejercicio 01, paso 5**: (a) `Class path of task ':core:compileKotlin' has changed from ... to ...`. Sin la línea del raíz, Kotlin se carga junto con `build-logic`; el jar de `build-logic` cambió, y con él el classpath de la tarea. (b) `Plugin [id: 'bitacora.kotlin-bse'] was not found in any of the following sources`: Gradle Core Plugins, Included Builds y Plugin Repositories; el tuyo es Included Builds. (c) Todas `UP-TO-DATE`. (d) Antes, tres (uno por módulo); ahora, uno (`bitacora.kotlin-base.gradle.kts`).

**Ejercicio 02, paso 4**: (a) `Unresolved reference 'libs'`, al compilar `:build-logic:compileKotlin`. En `build-logic/build.gradle.kts` existe porque es un script de un build que tiene catálogo; el plugin se compila sin saber dónde se aplicará. (b) `No value present`; con `orElseThrow`, tu mensaje, que dice qué falta: mejor ese. Salta al configurar `core` (`An exception occurred applying plugin request [id: 'bitacora.kotlin-library']`), no al compilar `build-logic`. (c) `The Kotlin Gradle plugin was loaded multiple times in different subprojects`. Con `alias(libs.plugins.kotlin.jvm) apply false` desaparece: Kotlin se carga en el raíz y `data` lo encuentra ahí al aplicar serialization. (d) Tres líneas (`plugins { id("bitacora.kotlin-library") }`); en la semana 10, unas 20.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Primer convention plugin (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 13 |
| `build-logic` sin versiones escritas; la base sin nada propio de un módulo (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Catálogo y composición (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Los plugins de encima no repiten la base; serialization solo en `data` (revisión del código) | 3 |
| `NOTAS.md`: paso 4 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora con build-logic

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 6 tests propios del build, esperados escritos a mano: ejecución, Java 21, warnings, cambio de plugin sin recompilar, sin repetición, JUnit desde la convención, Kotlin una vez | 25 |
| Convention plugins | Una base y un plugin por tipo de módulo; ningún `if` sobre el nombre del módulo; mensaje propio si falta una entrada del catálogo | 25 |
| Catálogo y raíz | Ninguna versión en `build-logic`; un solo catálogo para los dos builds; raíz con `apply false` | 10 |
| Adaptación al dominio | Módulos con tu código de las semanas 1-10, cada uno con un plugin y solo lo suyo; README que explica cada plugin y la línea del raíz | 20 |
| Entrega | Rama `week-11`, pull request con salida de `test` y de `:app:run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| Una versión escrita a mano en `build-logic` | -5 cada una |
| Un módulo que repite algo que ya pone su convention plugin | -5 cada uno |
| `if (project.name ...)` dentro de un convention plugin | -10 |
| Raíz sin `kotlin-jvm` con `apply false` | -5 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-11` → `main` en el repositorio del aprendiz.

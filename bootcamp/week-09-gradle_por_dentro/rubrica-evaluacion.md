# Rúbrica de Evaluación — Semana 09: Gradle por dentro

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: el build de la Bitácora |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Cuáles son las tres fases de un build y qué se hace en cada una? | 10 |
| 2 | ¿Por qué un `println` suelto en `build.gradle.kts` sale aunque pidas otra tarea? | 10 |
| 3 | ¿Qué ventaja tiene `tasks.register` sobre `tasks.create`? | 10 |
| 4 | ¿Qué diferencia hay entre `dependsOn`, `finalizedBy` y una dependencia implícita? | 10 |
| 5 | ¿Cómo decide Gradle que una tarea está `UP-TO-DATE`? | 10 |
| 6 | ¿Qué pasa si una entrada de una tarea propia no está anotada? | 10 |
| 7 | ¿Qué diferencia hay entre `UP-TO-DATE` y `FROM-CACHE`, y qué hace falta para el segundo? | 10 |
| 8 | ¿Qué guarda la caché de configuración y por qué `project` en un `doLast` la rompe? | 10 |
| 9 | ¿Qué es un archivo reproducible y qué cambió en Gradle 9? | 10 |
| 10 | ¿Por qué un test de build copia el proyecto de ejemplo a una carpeta temporal? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. **Inicialización** (lee `settings.gradle.kts` y decide qué proyectos hay), **configuración** (ejecuta los `build.gradle.kts`, registra tareas y arma el grafo) y **ejecución** (corre las tareas pedidas y sus dependencias).
2. Porque la fase de configuración ejecuta el archivo entero siempre, antes de decidir qué tareas se ejecutan. Solo lo que va dentro de `doFirst`, `doLast` o `@TaskAction` es de la fase de ejecución.
3. Es perezoso: el bloque de configuración solo corre si la tarea entra en el grafo. `create` la crea y configura siempre, y en un build grande eso cuesta segundos.
4. `dependsOn`: la otra tarea se ejecuta **antes**. `finalizedBy`: se ejecuta **justo después**, aunque pidas solo la primera. La implícita se deduce al usar la salida de una tarea como entrada de otra (`from(stage)`): no hay que declararla.
5. Calcula una huella de las entradas (valores y archivos) y mira las salidas. Si coinciden con las de la última ejecución, la tarea no hace nada.
6. Gradle no sabe que influye: si cambia, la tarea sigue `UP-TO-DATE` y deja un resultado viejo. Por eso una entrada sin anotar es un *bug*.
7. `UP-TO-DATE`: lo que hay en **tu** `build/` ya vale. `FROM-CACHE`: se restauró de la caché de resultados lo que otra ejecución produjo con las mismas entradas, aunque hayas borrado `build/`. Hace falta `org.gradle.caching=true` (o `--build-cache`) y `@CacheableTask` en las tareas propias.
8. El grafo de tareas ya armado, para saltar la fase de configuración. Las acciones corren después de guardarlo y no pueden depender del `Project`: Gradle falla con `Invocation of 'Task.project' ... at execution time is unsupported`. La solución: leer el valor al configurar y capturarlo.
9. Uno que, con las mismas entradas, da siempre los mismos bytes (mismo SHA-256). Desde Gradle 9.0, `Jar` y `Zip` lo son por defecto (fechas fijas, orden determinista, permisos fijos); basta con no deshacerlo con `isPreserveFileTimestamps = true`.
10. Para que cada test empiece sin `build/`, sin caché y sin cambios de otros tests, y para poder modificar archivos (la fecha de uno, una propiedad) sin tocar el proyecto real.

**Ejercicio 01, paso 7**: (a) `--dry-run` muestra `:stage`, `:bundle`, `:announce`, `:release` (según lo que pidas), todas `SKIPPED`: no se ejecuta nada. (b) La segunda ejecución es `UP-TO-DATE`; `notes.txt` no es un `.csv`, no es una entrada de `stage`, y sigue `UP-TO-DATE`; `a.csv` sí lo es, y se ejecuta. (c) Con una ruta de texto, Gradle no ve la relación: `stage` no se ejecuta, `bundle` queda `NO-SOURCE` y no hay zip. (d) Un `println` suelto sale en la fase de configuración, antes de las tareas, en cada ejecución; dentro de `doLast`, solo cuando la tarea se ejecuta.

**Ejercicio 02, paso 6**: (a) `Invocation of 'Task.project' by task ':bad' at execution time is unsupported with the configuration cache.` (b) `val n = project.name` fuera del `doLast` y `println(n)` dentro. (c) `Reusing configuration cache` / `Configuration cache entry reused`. (d) Las acciones se ejecutan sin configurar el proyecto, a partir del grafo guardado; el `Project` no existe en ese momento.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Tareas y dependencias (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 13 |
| `register` en vez de `create`, grupo y descripción, rutas con `layout` (revisión del código) | 3 |
| `NOTAS.md`: paso 7 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Tareas con entradas y salidas (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Entradas y salidas anotadas con `@get:`, clase y propiedades `abstract` (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — El build de la Bitácora

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 6 tests propios, esperados escritos a mano: contenido de `build-info.properties`, `run`, reproducibilidad, regeneración sin recompilar, propiedad ausente, caché de configuración | 25 |
| Tarea propia | `GenerateBuildInfo` con entradas y salidas anotadas; `register`; grupo y descripción; salida en `layout.buildDirectory` | 25 |
| Propiedades y caché | `providers.gradleProperty`, mensaje exacto si falta; `printVersion` sin `project` en la acción; caché de configuración reutilizada | 10 |
| Adaptación al dominio | Versión y dominio propios, `Main.kt` de la Bitácora leyendo `build-info.properties`; README adaptado | 20 |
| Entrega | Rama `week-09`, pull request con salida de `test` y de `run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `tasks.create` en vez de `register` | -5 cada uno |
| `project` dentro de `doLast` o `@TaskAction` | -10 |
| Ruta `"build/..."` escrita a mano | -5 cada una |
| Entrada o salida sin anotar | -5 cada una |
| Archivos reproducibles desactivados | -10 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-09` → `main` en el repositorio del aprendiz.

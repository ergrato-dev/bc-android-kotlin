# Rúbrica de Evaluación — Semana 10: Multi-módulo y version catalogs

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora en tres módulos |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué decide `settings.gradle.kts` y qué significa la ruta `:core:test`? | 10 |
| 2 | Da dos ventajas y un coste de dividir un proyecto en módulos | 10 |
| 3 | ¿Cuándo usas `api` y cuándo `implementation`? | 10 |
| 4 | ¿Qué diferencia hay entre el classpath de compilación y el de ejecución? | 10 |
| 5 | ¿Por qué cambiar el cuerpo de una función de `core` no recompila `data` ni `app`? | 10 |
| 6 | ¿Qué es un ciclo entre módulos, cómo lo detecta Gradle y cómo se rompe? | 10 |
| 7 | ¿Qué cuatro secciones tiene `libs.versions.toml` y para qué sirve cada una? | 10 |
| 8 | ¿Qué hace `apply false` en el raíz y qué avisa Gradle si falta? | 10 |
| 9 | ¿Qué hace `FAIL_ON_PROJECT_REPOS` y por qué es una medida de seguridad? | 10 |
| 10 | ¿Qué configuración sigue repetida después del catálogo y qué la resuelve mejor que `subprojects { }`? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Qué módulos forman el build (con `include`), el nombre del proyecto y los repositorios. `:core:test` es la tarea `test` del módulo `core`; `:` es la raíz.
2. Ventajas: **fronteras** (un módulo no puede usar lo que no declara), **compilación más rápida** y pruebas aisladas. Coste: cada módulo **repite configuración** (plugin, JDK, pruebas).
3. `api` si un tipo de esa dependencia aparece en la **API pública** del módulo (firmas); `implementation` si es un detalle interno. Por defecto, `implementation`.
4. El de compilación (`compileClasspath`) es lo que el módulo ve al compilar; el de ejecución (`runtimeClasspath`), lo que lleva al ejecutar. `implementation` oculta la dependencia solo en el de compilación de quien depende de ti.
5. Gradle compara la **interfaz pública** de `core`, no su código: si solo cambió un cuerpo, la interfaz es la misma y los demás quedan `UP-TO-DATE`.
6. Dos módulos que se necesitan mutuamente. Gradle falla con `Circular dependency between the following tasks`. Se rompe moviendo lo común a un tercer módulo, o invirtiendo la dependencia con una interfaz.
7. `[versions]` (nombres de versiones), `[libraries]` (módulos con su versión o sin ella si la fija un BOM), `[bundles]` (grupos de bibliotecas) y `[plugins]` (plugins con su versión).
8. Declara el plugin con su versión en el raíz sin aplicarlo, para que los módulos lo apliquen sin repetir la versión. Si falta, Kotlin se carga una vez por módulo y avisa `The Kotlin Gradle plugin was loaded multiple times in different subprojects`.
9. Hace fallar el build si un módulo declara **sus propios** repositorios: solo valen los de `settings.gradle.kts`. Importa porque de dónde se descarga el código que ejecutas es una decisión de seguridad: un solo sitio que revisar.
10. Quedan repetidos `kotlin { jvmToolchain(21) ... }`, `tasks.test` y las dependencias de prueba. Un **convention plugin** (semana siguiente) lo comparte con una línea por módulo, que se puede leer, probar y versionar; `subprojects { }` lo mezcla en el raíz, donde los módulos no lo ven.

**Ejercicio 01, paso 4**: (a) `Cannot access class 'Observation'. Check your module classpath for missing or conflicting dependencies.`: con `implementation`, `app` no ve `core` al compilar. (b) `Circular dependency between the following tasks`; se rompe moviendo lo común a un tercer módulo o con una interfaz. (c) Con un cambio de cuerpo se ejecuta `:core:compileKotlin` y `:data` y `:app` quedan `UP-TO-DATE`; con una función pública nueva, las tres se ejecutan: cambió la interfaz. (d) En `compileClasspath` aparecen `project ':data'` y `project ':core'` y no kotlinx-serialization; en `runtimeClasspath` también kotlinx-serialization.

**Ejercicio 02, paso 5**: (a) Falla **al compilar el script**, antes de ejecutar ninguna tarea: `Unresolved reference 'bomm'`. (b) `The Kotlin Gradle plugin was loaded multiple times in different subprojects...`, y propone declararlo en el raíz o añadir `apply false`. (c) `Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file '...'`. (d) Antes, uno por módulo que lo usara (aquí, `core` y `data`); ahora, uno solo (`libs.versions.toml`).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Módulos y dependencias (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 13 |
| `api` solo donde un tipo sale en la API, sin dependencia directa de `app` a `core` (revisión del código) | 3 |
| `NOTAS.md`: paso 4 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Version catalog y repositorios (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Catálogo completo, plugins en el raíz con `apply false`, repositorios solo en `settings` (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora en tres módulos

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 6 tests propios del build, esperados escritos a mano: ejecución, `core` sin dependencias, sin filtración de serialization, cambio de cuerpo, versiones una vez, repositorios | 25 |
| Estructura | Tres módulos con dependencias `app → data → core`; `api` solo donde un tipo sale en la API; sin ciclos | 25 |
| Catálogo y repositorios | Ninguna versión escrita a mano; plugins en el raíz; repositorios centralizados con `FAIL_ON_PROJECT_REPOS` | 10 |
| Adaptación al dominio | Código de las semanas 1-8 repartido por los módulos con sentido; README con el informe de dependencias explicado | 20 |
| Entrega | Rama `week-10`, pull request con salida de `test` y de `:app:run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `core` depende de otro módulo | -10 |
| Una versión escrita a mano en un `build.gradle.kts` | -5 cada una |
| `repositories { }` en un módulo | -5 cada uno |
| `api` donde bastaba `implementation` | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-10` → `main` en el repositorio del aprendiz.

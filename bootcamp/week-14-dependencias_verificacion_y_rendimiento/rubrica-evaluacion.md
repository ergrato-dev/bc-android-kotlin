# Rúbrica de Evaluación — Semana 14: Dependencias, verificación y rendimiento

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora verificada y rápida |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es una dependencia transitiva y qué significan `->`, `(c)` y `(*)` en el árbol de `dependencies`? | 10 |
| 2 | Tres dependencias piden versiones distintas de `kotlin-stdlib`. ¿Cuál usa Gradle y qué riesgo tiene esa regla? | 10 |
| 3 | ¿Qué te dice `dependencyInsight` que no te dice `dependencies`? | 10 |
| 4 | ¿Qué diferencia hay entre añadir una dependencia y añadir una restricción, y para qué sirve `because`? | 10 |
| 5 | ¿Por qué `annotations` se excluye de `runtimeClasspath` y no de todas las configuraciones? | 10 |
| 6 | ¿Qué problema resuelve la verificación de dependencias que no resuelve pinar versiones? | 10 |
| 7 | ¿Qué cubre `verification-metadata.xml` en un build con `build-logic`, y por qué los plugins son lo más delicado? | 10 |
| 8 | Un sha256 deja de coincidir sin que hayas cambiado nada. ¿Qué haces y qué no haces? | 10 |
| 9 | ¿Qué fase acelera la caché de configuración, cuál `org.gradle.parallel`, y cómo compruebas que están activas? | 10 |
| 10 | ¿Qué diferencia hay entre `--profile` y `--scan`, y cómo se mide bien un cambio de rendimiento? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Una que no declaras tú, sino una de tus dependencias (en su `.pom` o `.module`). `a:1.0 -> 2.0`: se pidió la 1.0 y se usa la 2.0. `(c)`: una restricción, no una dependencia; los BOMs aportan restricciones. `(*)`: ya aparece más arriba con sus dependencias.
2. La más alta: 2.4.21, frente a 2.2.20 (coroutines) y 2.4.10 (serialization). Es una apuesta por la compatibilidad hacia atrás: si la versión nueva quita algo que la vieja tenía, el error sale al ejecutar (`NoSuchMethodError`), no al compilar.
3. Para un solo módulo, en una configuración: qué versión ganó, por qué (`By conflict resolution`, `By constraint` y su motivo) y quién pidió cada versión.
4. Una dependencia añade el módulo al classpath. Una restricción solo fija su versión si alguien lo trae; si nadie lo trae, no hace nada. `because` deja escrito el motivo en el build y en `dependencyInsight` (`By constraint: <motivo>`). Sin él solo sale `By constraint`, y la restricción parece un capricho.
5. Porque `annotations` hace falta para compilar, pero no para ejecutar. Excluida de todas, la compilación falla: `e: java.lang.RuntimeException: Exception while generating code for public constructor ...`. Las exclusiones van en la configuración más concreta posible.
6. Que lo que llega sea lo que se publicó. Una versión pinada es un nombre; un repositorio comprometido, un espejo o un proxy pueden servir otro archivo con ese nombre. El sha256 apuntado en el repositorio lo detecta.
7. Todos los artefactos (jar, `.pom`, `.module`) que usa el build, también los plugins y lo que traen. En un build compuesto, el archivo del raíz cubre también `build-logic` (`gradle-kotlin-dsl-plugins`). Los plugins son lo más delicado porque su código se ejecuta al configurar, con tus permisos, en tu máquina y en el CI.
8. Parar, no regenerar el archivo (eso sería aceptar el artefacto cambiado) y no usar `lenient` para salir del paso. Averiguar de dónde viene: qué repositorio, qué proxy o espejo, si la caché de `~/.gradle` está dañada. Comparar con la suma que publica el repositorio oficial y avisar al equipo.
9. La caché de configuración se salta la fase de configuración; `org.gradle.parallel`, en la ejecución, ejecuta a la vez tareas de proyectos que no dependen entre sí. `./gradlew help --info` imprime `configurationCache=true` y `parallelProjectExecution=true`; y con la caché, la segunda ejecución dice `Configuration cache entry reused.`
10. `--profile` deja un informe local en `build/reports/profile`. `--scan` es mucho más detallado, pero sube los datos del build a un servidor externo: en el proyecto de un cliente, no sin permiso. Para medir bien: el mismo caso, varias veces, tirando la primera, y un cambio cada vez.

**Ejercicio 01, paso 6**: (a) 2.4.21 la pide el build (plugin de Kotlin), 2.2.20 `kotlinx-coroutines-core-jvm` 1.11.0 y 2.4.10 `kotlinx-serialization-core-jvm`/`json-jvm` 1.12.0. Es razonable porque `kotlin-stdlib` mantiene la compatibilidad hacia atrás; no lo sería con una biblioteca que rompe su API entre versiones. (b) `Conflict found for module 'org.jetbrains.kotlin:kotlin-stdlib': between versions 2.4.21, 2.2.20 and 2.4.10` y el de `org.jetbrains:annotations` (26.1.0, 13.0 y 23.0.0). (c) El motivo: `dependencyInsight` solo dice `- By constraint`. (d) La compilación falla en el plugin de serialization (`Exception while generating code for public constructor`): el compilador necesita las anotaciones.

**Ejercicio 02, paso 6**: (a) 60 componentes con `cbor`; tuyos, los que declaras: dos BOMs, `coroutines-core`, `serialization-json`, `serialization-cbor` y la restricción de `annotations`. El resto son transitivas y, sobre todo, los plugins de Kotlin y serialization con sus dependencias: se ejecutan en tu máquina al configurar. (b) `BUILD SUCCESSFUL` con un aviso de verificación. Sirve para ver todos los fallos de una vez mientras actualizas; nunca en el repositorio ni en el CI. (c) Sí, siguen. Hay que borrarlas a mano, y el diff lo revisa el pull request. (d) Parar, no regenerar, no usar `lenient`. Mirar de qué repositorio vino, comparar con la suma oficial, limpiar la caché de ese artefacto si está dañada, y avisar.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Conflictos y restricciones (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Versión en el catálogo; `because` con un motivo real; la exclusión solo en `runtimeClasspath` (revisión del código) | 3 |
| `NOTAS.md`: pasos 1 y 6 explicados | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Verificar lo que descargas (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| Archivo generado por Gradle con `sha256`; en el diff de `cbor` solo entra `cbor` (revisión del código) | 3 |
| `NOTAS.md`: paso 6 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora verificada y rápida

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings, con la caché de configuración activada | 10 |
| Tests | Mínimo 6 nuevos, esperados escritos a mano: plugin alterado, dependencia sin apuntar, caché de configuración reutilizada, build compatible, parámetros activos, `kotlin-stdlib` en `app` | 25 |
| Verificación | `gradle/verification-metadata.xml` con `sha256`, cubre `build-logic`, sin `lenient` ni entradas a mano | 20 |
| Rendimiento | Caché de configuración y paralelo en `gradle.properties`, sin problemas silenciados; mediciones de antes y después en el README | 15 |
| Adaptación al dominio | Tu dominio en la salida de `app`; README con lo que cubre la verificación y qué harías si un sha256 deja de coincidir | 20 |
| Entrega | Rama `week-14`, pull request con `test`, mediciones y el diff de la verificación | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `org.gradle.dependency.verification=lenient` u `off` en el repositorio | -20 |
| Un sha256 cambiado a mano para que pase | -20 |
| `org.gradle.configuration-cache.problems=warn` para silenciar problemas | -10 |
| Mediciones inventadas o sin el comando que las produjo | -10 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-14` → `main` en el repositorio del aprendiz.

# Rúbrica de Evaluación — Semana 12: Plugins propios

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: la Bitácora con su plugin |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es un plugin binario y cuándo lo prefieres a un precompiled script plugin? | 10 |
| 2 | ¿Qué hace `gradlePlugin { plugins { register(...) } }` y qué deja en el jar? | 10 |
| 3 | ¿En qué momento se ejecuta `apply` y por qué no se puede leer ahí `extension.domain.get()`? | 10 |
| 4 | ¿Qué diferencia hay entre `convention(project.version.toString())` y `convention(project.provider { project.version.toString() })`? | 10 |
| 5 | ¿Por qué la tarea se conecta con `set(extension.domain)` y qué pasa si el usuario no da `domain`? | 10 |
| 6 | ¿Para qué sirve `pluginManager.withPlugin("java")` y qué pasa sin él en un proyecto sin `java`? | 10 |
| 7 | ¿Qué comprueba `ProjectBuilder`, qué comprueba TestKit y qué hace `withPluginClasspath()`? | 10 |
| 8 | ¿Qué hace `validatePlugins` y por qué una propiedad sin anotación es un error? | 10 |
| 9 | ¿Qué es el marcador de un plugin y cómo pasa Gradle de `id(...) version "..."` al jar? | 10 |
| 10 | ¿Qué cambia al declarar `pluginManagement { repositories { } }` y por qué no se republica una versión? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Una clase que implementa `Plugin<Project>` y configura el proyecto en `apply`. Se prefiere cuando el plugin se **configura** (una extensión), tiene lógica propia, necesita pruebas por partes o se va a publicar para otros builds. Para decisiones fijas del equipo basta un precompiled script plugin.
2. Asocia un id (`bitacora.build-info`) con una clase (`BuildInfoPlugin`). En el jar escribe `META-INF/gradle-plugins/bitacora.build-info.properties` con `implementation-class=BuildInfoPlugin`: así Gradle encuentra la clase a partir del id. Al publicar, genera también el marcador.
3. Al aplicarse el plugin, en el bloque `plugins { }`: **antes** de que el build del usuario asigne `version` o escriba `buildInfo { }`. La propiedad todavía está vacía: `Cannot query the value of extension 'buildInfo' property 'domain' because it has no value available.`
4. La primera lee la versión al aplicar el plugin, cuando todavía es `unspecified`, y el archivo dice `version=unspecified`. La segunda es perezosa: la lambda se ejecuta cuando alguien pide el valor, después de `version = "..."`.
5. `set(provider)` guarda de dónde sacar el valor; Gradle lo lee justo antes de ejecutar la tarea, cuando el usuario ya escribió todo. Si falta `domain`, Gradle valida la tarea antes de ejecutarla: `property 'domain' doesn't have a configured value`.
6. Ejecuta su bloque cuando se aplica `java` (antes o después del plugin), y nunca si no se aplica. Sin él, en un proyecto sin `java`: `Extension of type 'SourceSetContainer' does not exist`.
7. `ProjectBuilder` crea un proyecto en memoria: comprueba que existen la extensión y la tarea y los valores por defecto, sin ejecutar tareas. TestKit ejecuta Gradle de verdad: archivos generados, `UP-TO-DATE`, errores, caché de configuración. `withPluginClasspath()` da al build de prueba el plugin sin publicar, gracias a `pluginUnderTestMetadata`.
8. Revisa que cada propiedad de las clases de tareas tenga su anotación (`@Input`, `@OutputDirectory`, `@Internal`...). Sin anotación, Gradle no ve esa entrada: la tarea quedaría `UP-TO-DATE` aunque cambie. Forma parte de `check`, así que `build` falla.
9. Un POM sin código con coordenadas fijas, `<id>:<id>.gradle.plugin:<versión>`, cuya dependencia es el artefacto real (`dev.bitacora:build-info-plugin:1.0.0`). Gradle busca el marcador en los repositorios de `pluginManagement`, sigue su dependencia hasta el jar y, dentro, el descriptor le da la clase.
10. Sustituye al Plugin Portal: Gradle solo busca en los repositorios declarados (es una decisión de seguridad). Una versión publicada no se cambia porque quien ya la usa recibiría código distinto sin enterarse; con un repositorio de carpeta nada lo impide, los repositorios públicos lo rechazan.

**Ejercicio 01, paso 5**: (a) Falla `theFileHasTheVersionAndTheDomain` (y los que leen el archivo): dice `version=unspecified`, porque la versión se lee en `apply`, antes de `version = "1.2.0"`. (b) `Cannot query the value of extension 'buildInfo' property 'domain' because it has no value available.`; `apply` se ejecuta en el bloque `plugins { }`, antes que el resto del script. (c) `Type 'GenerateBuildInfo' property 'domain' is missing an input or output annotation`; sin el aviso, cambiar el dominio no volvería a ejecutar la tarea. (d) Fallan todos los tests que no aplican `java`, también los dos unitarios: `Extension of type 'SourceSetContainer' does not exist`. Solo pasa `withJavaTheFileEndsUpInTheResources`.

**Ejercicio 02, paso 4**: (a) Coordenadas `bitacora.build-info:bitacora.build-info.gradle.plugin:1.0.0`, empaquetado `pom` y una dependencia a `dev.bitacora:build-info-plugin:1.0.0`; ningún código. Hace falta porque el id no dice dónde está el jar: el marcador hace de traducción, y un jar puede tener varios ids. (b) En `Gradle Central Plugin Repository`, el Plugin Portal, y falla: el plugin no está ahí. (c) Sale la descripción nueva: quien usaba la `1.0.0` recibe otro código sin cambiar nada. (d) `~/.m2/repository` lo lee cualquier build de la máquina que declare `mavenLocal()`: un estado global que no está en el repositorio del proyecto, y que cambia según lo que hayas publicado antes.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Un plugin con extensión y tarea (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew build`: 7 de 7 tests y `validatePlugins` en verde | 13 |
| Ningún `.get()` en `apply`; la versión por defecto con `provider`; recursos dentro de `withPlugin` (revisión del código) | 3 |
| `NOTAS.md`: paso 5 explicado | 4 |
| **Subtotal** | **20** |

### Ejercicio 02 — Publicar y consumir (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 13 |
| El consumidor solo declara el repositorio local; ninguna `mavenLocal()` (revisión del código) | 3 |
| `NOTAS.md`: paso 4 explicado | 4 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — La Bitácora con su plugin

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings, `validatePlugins` incluido | 10 |
| Tests | Mínimo 2 unitarios del plugin y 6 del build, esperados escritos a mano: salida de `app`, contenido del jar, versión sin recompilar, `domain` ausente, solo los programas, `:build-logic:test` | 25 |
| Plugin | Extensión con valores por defecto perezosos, tarea conectada sin `.get()`, `withPlugin` para `java`, grupo y descripción | 25 |
| Composición y valores | Aplicado por `bitacora.kotlin-application` por su clase; la versión una vez en `gradle.properties`; el dominio en `app` | 10 |
| Adaptación al dominio | `app` imprime tu dominio; README que explica por qué es un plugin binario y por qué se aplica por su clase | 20 |
| Entrega | Rama `week-12`, pull request con salida de `test` y de `:app:run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `.get()` o `afterEvaluate` en `apply` | -10 |
| La versión escrita en más de un sitio | -5 |
| Una propiedad de tarea sin anotación (aunque se haya silenciado `validatePlugins`) | -5 cada una |
| `core` o `data` con `generateBuildInfo` | -5 |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-12` → `main` en el repositorio del aprendiz.

# Semana 01 — Kotlin, toolchain y tipos

> Antes de escribir una app hay que saber qué pasa entre tu `.kt` y un programa funcionando, quién es Gradle, y por qué en Kotlin un `String` y un `String?` son tipos distintos. Esta semana es el ciclo que repetirás las cuarenta y una siguientes.

No hay clases propias, ni colecciones, ni coroutines. Hay tipos, `val`/`var`, nulos, texto formateado, una `data class` y el recorrido completo de `./gradlew build` visto por dentro.

---

## 🎯 Objetivos de la Semana

Al finalizar esta semana serás capaz de:

- **Describir** el camino de un `.kt` a la JVM y **abrir** un `.class` con `javap`
- **Usar** `./gradlew build`, `test` y `run`, y **leer** un `build.gradle.kts` mínimo
- **Distinguir** un error de compilación de uno de ejecución
- **Elegir** el tipo correcto para cada dato y **convertir** entre tipos de forma explícita
- **Preferir** `val` sobre `var` y **tratar** un `String?` con `?.` y `?:`
- **Explicar** el desbordamiento de `Int` y por qué `Double` no se compara con `==`
- **Formatear** salida con `String.format(Locale.ROOT, ...)` y separar `println` de `System.err`
- **Modelar** datos con una `data class`

---

## 📋 Prerrequisitos

- Ninguna semana anterior: esta es la primera.
- JDK 21, Git y un editor, según [`docs/setup.md`](../../docs/setup.md). Verifica con `java -version`.
- Terminal básica y Git (`bc-linux-commandline`, `bc-github`).
- **No hace falta saber programar en Kotlin ni en Java.**

---

## 🗂️ Estructura de la Semana

```
week-01-kotlin_toolchain_y_tipos/
├── README.md                    # este archivo
├── rubrica-evaluacion.md        # cómo se evalúa la semana
├── 0-assets/                    # 4 diagramas SVG
├── 1-teoria/                    # 5 archivos, un concepto cada uno
├── 2-practicas/                 # 2 ejercicios con starter/ y tests
├── 3-proyecto/                  # la capa 0 de tu Bitácora: el core
├── 4-recursos/                  # webgrafía, ebooks, vídeos
└── 5-glosario/                  # los términos de la semana, A-Z
```

---

## 📝 Contenidos

### Teoría

| Archivo | Tema | Duración |
| ------- | ---- | -------- |
| [01 · Qué es Kotlin hoy](1-teoria/01-que-es-kotlin-hoy.md) | Bytecode, JVM, el mapa de Kotlin, versiones fijadas | 40 min |
| [02 · Del código al bytecode](1-teoria/02-del-codigo-al-bytecode.md) | `.kt`, `.class`, `.jar`, `javap`, errores de compilación y de ejecución | 40 min |
| [03 · Gradle mínimo](1-teoria/03-gradle-minimo.md) | Wrapper, `build.gradle.kts`, tareas, daemon | 50 min |
| [04 · Tipos, variables y nulos](1-teoria/04-tipos-variables-y-nulos.md) | Tipos básicos, `val`/`var`, conversiones, desbordamiento, `String?` | 40 min |
| [05 · Texto y salida](1-teoria/05-texto-y-salida.md) | Plantillas, `String.format`, `Locale`, stdout/stderr, `data class` | 40 min |

### Prácticas

| Ejercicio | Qué haces | Duración |
| --------- | --------- | -------- |
| [01 · Compila tu primer programa con Gradle](2-practicas/ejercicio-01-hola-toolchain/README.md) | 4 tests en verde, `javap`, un `-Werror` provocado | 1 h 30 min |
| [02 · Mide los tipos](2-practicas/ejercicio-02-tipos-y-limites/README.md) | Tamaños, división, desbordamiento, truncado, comparar decimales | 2 h |

### Proyecto

Eliges tu dominio, creas tu repositorio y escribes el `core` de tu Bitácora: una `data class`, un informe alineado con `String.format`, estadísticas y tests calculados a mano.
→ [3-proyecto/README.md](3-proyecto/README.md)

---

## ⏱️ Distribución del Tiempo (14 horas)

| Actividad | Tiempo |
| --------- | ------ |
| Teoría (5 archivos) | 3 h 30 min |
| Ejercicio 01 | 1 h 30 min |
| Ejercicio 02 | 2 h |
| Proyecto | 4 h 30 min |
| Recursos y glosario | 1 h 30 min |
| Autoevaluación (cuestionario de la rúbrica) | 1 h |
| **Total** | **14 h** |

---

## 🎩 Trucos y atajos

| Truco | Cómo |
| ----- | ---- |
| Probar una línea sin crear proyecto | <https://play.kotlinlang.org> |
| Ver el bytecode de tu función | `./gradlew build && javap -c -cp build/classes/kotlin/main MainKt` |
| Ejecutar solo un test | `./gradlew test --tests "MainTest.saludaPorNombre"` |
| Repetir los tests aunque nada cambió | `./gradlew test --rerun` |
| Ver por qué una tarea se ejecutó o no | `./gradlew build --info` |
| Ver todas las tareas | `./gradlew tasks` |
| Ver el árbol de dependencias | `./gradlew dependencies --configuration testRuntimeClasspath` |
| Comprobar tus versiones | `./gradlew --version` |

---

## 📌 Entregables

1. ✅ Ejercicio 01: `./gradlew test` con 4 de 4 en verde, y los pasos 5 y 6 hechos
2. ✅ Ejercicio 02: 5 de 5 en verde, y el paso 6 (error de tipos) hecho y deshecho
3. ✅ Repositorio `bc-android-kotlin-<tu-dominio>` creado, con el dominio escrito en su README
4. ✅ Proyecto: rama `week-01` con `build.gradle.kts`, `src/main` y al menos 5 tests calculados a mano, todos en verde
5. ✅ Pull request `week-01` → `main` con dominio, versiones de `./gradlew --version` y salida de los tests
6. ✅ Cuestionario de la [rúbrica](rubrica-evaluacion.md) respondido antes de mirar las respuestas

---

## ✅ Verificación

En cada `starter/` (ejercicios y proyecto):

```bash
./gradlew test
```

Tiene que terminar con `BUILD SUCCESSFUL`. Si algo falla antes de llegar a los tests (JDK, descarga de Gradle), la tabla de errores frecuentes de [03 · Gradle mínimo](1-teoria/03-gradle-minimo.md) cubre los casos habituales.

---

## 🔗 Navegación

| Anterior | Actual | Siguiente |
| -------- | ------ | --------- |
| — | **Semana 01** | [Semana 02](../week-02-control_funciones_y_errores/README.md) |

← [Volver al inicio del bootcamp](../../README.md)

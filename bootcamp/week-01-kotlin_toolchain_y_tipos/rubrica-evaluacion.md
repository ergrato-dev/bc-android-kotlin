# Rúbrica de Evaluación — Semana 01: Kotlin, toolchain y tipos

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: el `core` de la Bitácora |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué es el bytecode y qué papel tiene la JVM? | 10 |
| 2 | Nombra las etapas entre `Main.kt` y el programa corriendo, y el archivo que produce cada una. | 10 |
| 3 | `Unresolved reference: x` y `ArithmeticException: / by zero`: ¿cuál es de compilación y cuál de ejecución? ¿Por qué preferimos el primer tipo? | 10 |
| 4 | ¿Para qué sirve el wrapper (`gradlew`) y qué dos cosas fija en `gradle-wrapper.properties`? | 10 |
| 5 | En `build.gradle.kts`, ¿qué hacen `plugins`, `repositories` y `dependencies`? | 10 |
| 6 | ¿Por qué `val x: Int = 3.7` no compila? ¿Cómo obtienes `3` y cómo `3.7` como `Double`? | 10 |
| 7 | ¿Cuándo usas `val`, cuándo `var` y cuándo `const val`? | 10 |
| 8 | ¿Qué da `7 / 2`? ¿Y `Int.MAX_VALUE + 1`? ¿Cómo evitas el segundo? | 10 |
| 9 | ¿Qué diferencia hay entre `String` y `String?`, y qué hacen `?.` y `?:`? | 10 |
| 10 | ¿Por qué `String.format` lleva `Locale.ROOT` en un test? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Bytecode: instrucciones intermedias en archivos `.class`, independientes de la máquina. La JVM las carga, las interpreta y compila a código máquina las partes que se repiten (JIT).
2. Compilar (`kotlinc`, `.kt` → `.class`), empaquetar (`jar`, → `.jar`) y ejecutar (`java`, la JVM carga el `.class`/`.jar`).
3. `Unresolved reference` es de compilación; `ArithmeticException` es de ejecución. Preferimos los de compilación: saltan antes de que el programa llegue al usuario, sin necesidad de un caso de prueba que lo active.
4. Descarga y usa una versión exacta de Gradle sin instalarla a mano, igual para todos. Fija `distributionUrl` (la versión) y `distributionSha256Sum` (la suma de verificación de la descarga).
5. `plugins`: qué sabe hacer el proyecto (Kotlin/JVM, ejecutable). `repositories`: de dónde se bajan las librerías. `dependencies`: qué librerías usa y en qué ámbito (`testImplementation` solo para tests).
6. Kotlin no convierte números implícitamente: `Initializer type mismatch: expected 'Int', actual 'Double'`. `3.7.toInt()` da `3` (trunca); `3.7` ya es `Double`. Mejor que falle a compilar que perder el `.7` sin avisar.
7. `val` por defecto (no se reasigna). `var` cuando el valor cambia de verdad. `const val` para constantes conocidas al compilar, fuera de funciones.
8. `3` (división entera de dos `Int`). `Int.MAX_VALUE + 1` da `-2147483648` (da la vuelta, sin error). Se evita con `Math.addExact`, que lanza `ArithmeticException`, o usando `Long`.
9. `String` nunca es `null`; `String?` puede serlo y el compilador te obliga a decidir. `?.` ejecuta la llamada solo si no es `null` (si lo es, da `null`); `?:` da un valor alternativo cuando la izquierda es `null`.
10. Sin `Locale`, el formato depende de la configuración regional (coma o punto decimal). Un test pasaría en una máquina y fallaría en otra.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Compila tu primer programa con Gradle (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 4 de 4 en verde | 10 |
| Paso 5 hecho: sabe mostrar el `.class` generado y su bytecode con `javap` | 5 |
| Paso 6 hecho: ha provocado y arreglado un fallo por `allWarningsAsErrors` | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Mide los tipos (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 5 de 5 en verde | 14 |
| Paso 6 hecho: ha visto el error de tipos y sabe explicar por qué Kotlin no convierte solo | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — El `core` de la Bitácora

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin un solo warning (`allWarningsAsErrors` activo) | 10 |
| Tests | Mínimo 5 tests con valores calculados a mano; todos pasan | 25 |
| Tipos y nulos | `val` por defecto, `var` justificado, sin `!!`, constantes con `const val` | 15 |
| Salida | Cabecera, tabla alineada con `String.format(Locale.ROOT, ...)`, total y media con 2 decimales, media como división real | 20 |
| Adaptación al dominio | Nombres del dominio en inglés; cantidad y medida con sentido en ese dominio; README adaptado | 20 |
| Entrega | Rama `week-01`, pull request con dominio, versiones y salida de los tests | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `String.format` sin `Locale` | -10 |
| `!!` en el código | -5 cada uno |
| `var` donde `val` bastaba | -5 |
| Sin tests propios (solo el `header` del starter) | -15 |
| Test cuyo valor esperado es copiado de la salida del programa sin calcularlo (se pregunta) | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-01` → `main` en el repositorio del aprendiz.

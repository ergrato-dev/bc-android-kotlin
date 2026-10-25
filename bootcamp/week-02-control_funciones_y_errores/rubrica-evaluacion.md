# Rúbrica de Evaluación — Semana 02: Control de flujo, funciones y errores

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: reglas y validación del `core` |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre una expresión y una sentencia? Da un ejemplo con `if`. | 10 |
| 2 | ¿Por qué un `when` sin argumento con `n % 3 == 0` antes que `n % 15 == 0` falla para el 30? | 10 |
| 3 | ¿Qué valores recorren `1..4`, `1 until 4`, `4 downTo 1` y `0..10 step 5`? ¿Qué pasa con `4..1`? | 10 |
| 4 | ¿Cuándo usas `for` y cuándo `while`? Da un caso de cada uno. | 10 |
| 5 | ¿Qué hace un parámetro con valor por defecto y cuándo conviene llamar con argumento con nombre? | 10 |
| 6 | ¿Qué problema tiene una función `vararg` llamada sin argumentos y cómo lo resuelves? | 10 |
| 7 | Para `parseEntero("abc")`: ¿devuelves `null` o lanzas una excepción? ¿Y para `dividir(1.0, 0.0)`? Justifica. | 10 |
| 8 | ¿En qué se diferencian `require` y `check`? | 10 |
| 9 | ¿Qué devuelve `try { "12".toInt() } catch (e: NumberFormatException) { -1 }`? ¿Por qué se puede usar así? | 10 |
| 10 | Escribe los casos que probarías para `level(measure)` con umbrales 10 y 50. ¿Por qué esos? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Una expresión tiene valor (`val m = if (a > b) a else b`); una sentencia hace algo sin devolver un valor. En Kotlin `if` y `when` son expresiones, y como tales exigen `else`.
2. Porque `when` toma la primera rama que se cumple: 30 es múltiplo de 3, así que sale "Fizz" y nunca llega a la rama de 15. Va de lo más específico a lo más general.
3. `1..4` → 1, 2, 3, 4. `1 until 4` → 1, 2, 3. `4 downTo 1` → 4, 3, 2, 1. `0..10 step 5` → 0, 5, 10. `4..1` está vacío: no da error y no ejecuta el bucle.
4. `for` cuando recorres algo conocido (un rango, un texto, una lista): `for (i in 1..n)`. `while` cuando la parada depende de lo que pasa dentro: `while (d * d <= n)` buscando un divisor.
5. Si no lo pasas, usa el valor por defecto. Con nombre cuando hay varios parámetros del mismo tipo (dos `Double`) o quieres saltarte alguno: `level(12.0, highFrom = 20.0)`.
6. Es válida y el array llega vacío: una media daría `NaN` (0 entre 0). Se valida con `require(valores.isNotEmpty())`.
7. `parseEntero("abc")` devuelve `null`: un texto del usuario mal escrito es normal y quien llama decide. `dividir(1.0, 0.0)` lanza `IllegalArgumentException`: dividir entre cero viola el contrato, es un bug del que llama.
8. `require` valida argumentos y lanza `IllegalArgumentException`; `check` valida el estado interno y lanza `IllegalStateException`.
9. Devuelve `12`, porque `try` es una expresión: su valor es el último del bloque que se ejecutó. Si falla, el valor es el del `catch` (`-1`).
10. Normal (5.0 → LOW, 20.0 → MEDIUM, 60.0 → HIGH), en el límite (9.99 → LOW, 10.0 → MEDIUM, 49.99 → MEDIUM, 50.0 → HIGH), y de error (-1.0 y `NaN` lanzan). Los bugs viven en los límites (`>=` frente a `>`).

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Control de flujo (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 tests en verde | 15 |
| Sin `var` innecesarios ni bucles donde `when` basta (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Funciones y errores (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 6 de 6 en verde | 14 |
| `NOTAS.md`: paso 8 explicado (excepción, `null` o `Result` y por qué) | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Reglas y validación

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings (`allWarningsAsErrors`) | 10 |
| Tests | Mínimo 6 tests propios, calculados a mano; incluyen límites de `level`, un `assertFailsWith` y una línea inválida | 25 |
| Reglas | `level` con `when`, `require` con mensaje que dice qué valor llegó, sin `!!` ni `catch` vacíos | 20 |
| Lectura | `parseObservation` devuelve `null` en los 4 casos de línea inválida; `parseValid` y `countInvalid` coherentes | 20 |
| Adaptación al dominio | Umbrales y datos con sentido en el dominio; mensajes y README adaptados | 15 |
| Entrega | Rama `week-02`, pull request con salida de `test` y de `run` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `catch` vacío o `catch (e: Exception)` que traga el error | -10 |
| `!!` en el código | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| `String.format` sin `Locale.ROOT` | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-02` → `main` en el repositorio del aprendiz.

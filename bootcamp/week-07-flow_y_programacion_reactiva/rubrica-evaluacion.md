# Rúbrica de Evaluación — Semana 07: Flow y programación reactiva

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: Bitácora reactiva |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué significa que un `Flow` sea frío? Pon un ejemplo de lo que implica. | 10 |
| 2 | ¿Por qué `first()` y `take(n)` no dejan al origen emitiendo? | 10 |
| 3 | ¿Qué hace `flowOn` y por qué no se usa `withContext` dentro de `flow { }`? | 10 |
| 4 | ¿En qué se diferencian `buffer`, `conflate` y `collectLatest`? | 10 |
| 5 | ¿Qué errores captura `catch` y cuáles no? | 10 |
| 6 | ¿Qué es la transparencia de excepciones y qué la rompe? | 10 |
| 7 | ¿Por qué un `StateFlow` ignora un valor igual al anterior y qué consecuencia tiene para eventos? | 10 |
| 8 | ¿Por qué se usa `update { }` en lugar de `value = value + 1`, y qué precaución exige su bloque? | 10 |
| 9 | ¿Qué significa `WhileSubscribed(5_000)` y por qué se usa 5 segundos? | 10 |
| 10 | ¿Para qué sirve `flatMapLatest` y qué problema evita en una búsqueda? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. Que no hace nada hasta que alguien lo recoge, y que cada colector provoca una ejecución completa desde cero. Dos `collect` sobre el mismo flujo ejecutan dos veces el bloque `flow { }`.
2. Porque terminan el flujo en cuanto tienen lo que querían, y esa terminación cancela el `flow { }`: no sigue emitiendo hasta el final.
3. `flowOn` cambia el dispatcher de lo que tiene **por encima**. Dentro de `flow { }` no se cambia de contexto porque quien recoge debe poder confiar en que los valores llegan en su contexto (`Flow invariant is violated`); si varias coroutines emiten, `channelFlow`.
4. `buffer` guarda todos los valores en una cola para que el productor no espere; `conflate` descarta los intermedios y deja el último; `collectLatest` cancela el bloque del colector cuando llega un valor nuevo.
5. Solo las del `flow { }` y los operadores **anteriores** a él. No captura lo que lance el bloque de `collect` ni lo de después.
6. Que un flujo no debe esconder los errores del colector. La rompe un `try/catch` alrededor de `emit`; se usa `.catch { }`.
7. Porque compara por `equals` y solo emite si cambió: es un **estado**, no una secuencia de sucesos. Un evento repetido ("mostrar este mensaje") se perdería o se repetiría de forma incorrecta; para eventos, un `SharedFlow` u otra herramienta.
8. Porque `value = value + 1` lee y escribe por separado y varios hilos pierden incrementos; `update` lo hace como una operación atómica. Su bloque puede ejecutarse más de una vez, así que no debe tener efectos fuera de él.
9. El flujo de origen corre mientras haya suscriptores y 5 000 ms más tras irse el último. Una rotación de pantalla dura menos, así que el origen no se reinicia; si la app pasa a segundo plano, el origen se cancela y no gasta.
10. Cancela el trabajo de la consulta anterior cuando llega una nueva. Evita que una búsqueda lenta de `"a"` termine después de la de `"ab"` y pise el resultado correcto con uno viejo.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Flujos fríos (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 15 |
| Sin `GlobalScope`, sin `try/catch` alrededor de `emit` y sin tragarse `CancellationException` (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Flujos calientes y combinación (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 8 de 8 en verde | 14 |
| `NOTAS.md`: paso 7 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Bitácora reactiva

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 8 tests propios con `runTest`, calculados a mano; orden de llegada, `maxParallel`, fallo y tiempo agotado, `take(1)`, `watch`, `countByLevel` | 25 |
| Flujos | `StateFlow` con lista inmutable y `update`, `combine` en `watch`, `channelFlow` con una coroutine por fuente, `Semaphore` correcto | 25 |
| Cancelación y errores | `take(1)` cancela las pendientes; la cancelación no se convierte en un fallo; un fallo es un evento y no cancela a las demás | 10 |
| Adaptación al dominio | Fuentes, datos y mensajes con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-07`, pull request con salida de `test` y de `run`, y la decisión sobre `Logbook` frente a `LogbookStore` | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `GlobalScope` o `runBlocking` fuera de `main` | -10 |
| `Thread.sleep` o `delay` real en un test | -5 cada uno |
| Lista mutable dentro del `StateFlow`, o `value = value + x` en lugar de `update` | -10 |
| `try/catch` alrededor de `emit`, o `catch (e: Exception)` que se traga la `CancellationException` | -5 cada uno |
| Estado compartido sin protección (`var` mutado desde varias coroutines) | -10 |
| `!!` o `catch` vacío | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-07` → `main` en el repositorio del aprendiz.

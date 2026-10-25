# Rúbrica de Evaluación — Semana 06: Coroutines y concurrencia

## 📊 Distribución de Evidencias

| Tipo | Peso | Instrumento |
| ---- | ---- | ----------- |
| Conocimiento 🧠 | 30% | Cuestionario de autoevaluación (10 preguntas) |
| Desempeño 💪 | 40% | Ejercicios 01 y 02 verificados por `./gradlew test` |
| Producto 📦 | 30% | Proyecto semanal: sincronización asíncrona de la Bitácora |

**Nota mínima para aprobar**: 70% en cada tipo de evidencia.

---

## 🧠 Conocimiento (30%)

| # | Pregunta | Puntaje |
| - | -------- | ------- |
| 1 | ¿Qué diferencia hay entre `delay(100)` y `Thread.sleep(100)`? | 10 |
| 2 | ¿Qué devuelven `launch` y `async` y cuándo usas cada uno? | 10 |
| 3 | ¿Qué garantiza la concurrencia estructurada? Nombra dos garantías. | 10 |
| 4 | ¿Cuándo eliges `Dispatchers.IO` y cuándo `Dispatchers.Default`? | 10 |
| 5 | ¿Por qué un bucle de CPU no se detiene al cancelarlo y cómo lo arreglas? | 10 |
| 6 | ¿Por qué `catch (e: Exception)` alrededor de código `suspend` es peligroso? | 10 |
| 7 | ¿Qué diferencia hay entre `withTimeout` y `withTimeoutOrNull`? | 10 |
| 8 | ¿Cuándo usas `supervisorScope` en lugar de `coroutineScope`? | 10 |
| 9 | ¿Por qué `count++` desde varias coroutines pierde cuentas y qué lo arregla? | 10 |
| 10 | ¿Qué ventaja tiene `runTest` frente a una prueba con `delay` real? | 10 |

**Total**: 100 puntos → 30% de la nota final.

<details>
<summary>Respuestas</summary>

1. `delay` **suspende** la coroutine y libera el hilo para otras; además se puede cancelar. `Thread.sleep` **bloquea** el hilo y no se cancela.
2. `launch` devuelve un `Job` (trabajo sin resultado); `async` devuelve un `Deferred<T>`, y `await()` entrega el valor. `async` para calcular un valor, sobre todo varios a la vez; `launch` para efectos.
3. Que un scope no termina hasta que terminan sus hijas (no se pierde trabajo); que cancelar al padre cancela a las hijas; que el error de una hija sube al padre y cancela a las hermanas.
4. `IO` para esperar (red, disco, base de datos); `Default` para calcular (CPU), con tantos hilos como núcleos.
5. Porque la cancelación es cooperativa: solo se nota en un punto de suspensión. Se arregla con `ensureActive()`, `yield()` o comprobando `isActive` en cada vuelta.
6. Porque también atrapa `CancellationException`: la coroutine cancelada sigue como si nada. Se relanza antes (`catch (e: CancellationException) { throw e }`).
7. `withTimeout` lanza `TimeoutCancellationException`; `withTimeoutOrNull` devuelve `null`, ideal para un valor por defecto.
8. Cuando las hijas son independientes y el fallo de una no debe cancelar a las demás. El error espera en el `await` de esa hija.
9. Porque `count++` es leer, sumar y escribir, y dos hilos pueden leer el mismo valor. Lo arregla un `Mutex` (`withLock`) o un `AtomicInteger`.
10. Salta los `delay` con un reloj virtual: la prueba dura milisegundos y `currentTime` permite comprobar tiempos con valores exactos.

</details>

---

## 💪 Desempeño (40%)

### Ejercicio 01 — Suspend y scopes (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 tests en verde | 15 |
| Sin `GlobalScope`, sin `runBlocking` en las funciones, sin tragarse `CancellationException` (revisión del código) | 5 |
| **Subtotal** | **20** |

### Ejercicio 02 — Cancelación y estado compartido (20 puntos)

| Criterio | Puntaje |
| -------- | ------- |
| `./gradlew test`: 7 de 7 en verde | 14 |
| `NOTAS.md`: paso 8 explicado | 6 |
| **Subtotal** | **20** |

**Total Desempeño**: 40 puntos → 40% de la nota final.

---

## 📦 Producto (30%)

### Proyecto Semanal — Sincronización asíncrona

| Criterio | Descripción | Puntaje |
| -------- | ----------- | ------- |
| Compilación | `./gradlew build` sin warnings | 10 |
| Tests | Mínimo 8 tests propios con `runTest`, calculados a mano; paralelismo 1, 2 y 3, tiempo agotado, excepción con y sin mensaje, cancelación | 25 |
| Concurrencia | Fuentes a la vez, orden del resultado conservado, `Semaphore` correcto, el tiempo límite cuenta desde el arranque de la fuente | 25 |
| Cancelación y errores | La cancelación se propaga; un fallo no cancela a las demás; motivos exactos | 10 |
| Adaptación al dominio | Fuentes, datos y mensajes con sentido en el dominio; README adaptado | 20 |
| Entrega | Rama `week-06`, pull request con salida de `test` y de `run`, y la lista de fuentes caídas con su motivo | 10 |
| **Total** | | **100** |

### Penalizaciones

| Situación | Penalización |
| --------- | ------------ |
| No compila | 0 en Producto |
| `GlobalScope` | -10 |
| `Thread.sleep` o `delay` real en un test | -5 cada uno |
| `runBlocking` fuera de `main` | -5 cada uno |
| `catch (e: Exception)` o `runCatching` que se traga la `CancellationException` | -5 cada uno |
| Estado compartido sin protección (`var` mutado desde varias coroutines) | -10 |
| `!!` o `catch` vacío | -5 cada uno |
| Test cuyo valor esperado se copió de la salida sin calcular | -10 |
| Código copiado de otro dominio | 0 en Producto |

---

## 📅 Fecha de Entrega

- **Ejercicios**: al terminar cada uno; se comprueban con `./gradlew test`.
- **Proyecto**: 7 días después del inicio de la semana.
- **Formato**: pull request `week-06` → `main` en el repositorio del aprendiz.

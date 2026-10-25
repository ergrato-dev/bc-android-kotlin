# Ejercicio 02 — Cancelación y estado compartido

⏱️ 2 h · Teoría previa: 03, 04 y 05

## Objetivo

Compartir estado sin perder datos, limitar la concurrencia, aislar fallos y cancelar bien, comprobando cada cosa con un test.

## Pasos

Trabaja dentro de `starter/`, en `src/main/kotlin/Main.kt`.

1. **`SafeCounter`** con `Mutex`. El test lanza 100 coroutines de 1000 incrementos en `Dispatchers.Default`: tiene que dar 100 000. Quita el `Mutex` y ejecútalo varias veces.
2. **`produceSquares` y `sumOfSquares`**: un `Channel`, un `launch` que envía y `close()`. Quita el `close()` y mira qué pasa con el test (tendrás que cancelarlo).
3. **`runAll`** con `supervisorScope`: el fallo de la tarea 2 no tumba a las otras. Los resultados siguen el orden de las tareas.
4. **`withResource`**: `open`, el bloque y `close` **siempre**, también al cancelar. El cierre usa `delay`, así que necesita `NonCancellable`; el test comprueba que el cierre tardó 50 ms.
5. **`mapLimited`** con `Semaphore`: nunca más de `limit` a la vez. El test mide el máximo simultáneo y el tiempo (6 elementos, límite 2, 100 ms cada uno: 300 ms).
6. **`processAll`**: un bucle de CPU que se detiene al cancelarlo (`ensureActive`). Sin la comprobación, el test ve los 10 elementos.
7. **`errorScope`**: `SupervisorJob` más `CoroutineExceptionHandler`, que anota el mensaje en `errors`.
8. **`NOTAS.md`**: en `runAll`, cambia `supervisorScope` por `coroutineScope` y ejecuta el test. Responde con tus palabras: (a) ¿qué ocurre y por qué?, (b) ¿en qué momento se ve el error de la tarea 2?, (c) ¿qué le pasa a la tarea 3? Después devuelve el `supervisorScope`.
9. **Ejecuta** `./gradlew test` (7 de 7) y `./gradlew run`.

## 🧯 Si algo sale mal

| Síntoma | Qué hacer |
| --- | --- |
| El contador da menos de 100 000 | Los incrementos no pasan todos por `withLock` (o `value()` lee sin candado) |
| El test del canal no termina | Falta `channel.close()` |
| `runAll` lanza `IllegalStateException: boom` | Usas `coroutineScope`, o no capturas el error de `await()` |
| `withResource` termina en 100 ms, sin `close` | El cierre está fuera de `withContext(NonCancellable)` |
| `mapLimited` da `peak` = 6 | El `withPermit` no rodea a `transform` |
| `processAll` ve 10 elementos | Falta `coroutineContext.ensureActive()` en cada vuelta |
| `errorScope` no anota nada | El `CoroutineExceptionHandler` va en el contexto del scope, no en el `launch` |

## Entrega

Los 7 tests en verde y `NOTAS.md` con la respuesta del paso 8.
